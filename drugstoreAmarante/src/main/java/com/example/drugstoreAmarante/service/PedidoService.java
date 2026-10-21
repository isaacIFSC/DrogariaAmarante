package com.example.drugstoreAmarante.service;

import com.example.drugstoreAmarante.domain.CarrinhoItem;
import com.example.drugstoreAmarante.domain.ItemPedido;
import com.example.drugstoreAmarante.domain.Pedido;
import com.example.drugstoreAmarante.domain.Produto;
import com.example.drugstoreAmarante.domain.StatusPedido;
import com.example.drugstoreAmarante.dto.CarrinhoResumoResponse;
import com.example.drugstoreAmarante.dto.FinalizarPedidoRequest;
import com.example.drugstoreAmarante.dto.PedidoResponse;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.repository.PedidoRepository;
import com.example.drugstoreAmarante.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoService carrinhoService;
    private final AuthSessionService authSessionService;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PedidoService(
        PedidoRepository pedidoRepository,
        ProdutoRepository produtoRepository,
        CarrinhoService carrinhoService,
        AuthSessionService authSessionService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.carrinhoService = carrinhoService;
        this.authSessionService = authSessionService;
    }

    @Transactional
    public PedidoResponse finalizarPedido(FinalizarPedidoRequest req, HttpSession session) {
        validarDadosCheckout(req);

        User userLogado = authSessionService.currentUser(session);
        if (userLogado == null || !authSessionService.isCliente(session)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Entre na sua conta para finalizar o pedido.");
        }

        List<CarrinhoItem> itensCarrinho = carrinhoService.obterItens(session);
        if (itensCarrinho.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seu carrinho está vazio. Adicione produtos antes de finalizar.");
        }

        CarrinhoResumoResponse resumo = carrinhoService.obterResumo(session);

        // Validar estoque de cada produto
        for (CarrinhoItem item : itensCarrinho) {
            Produto produto = item.getProduto();
            if (produto.getEstoque() < item.getQuantidade()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estoque insuficiente para o produto: " + produto.getNome() + ". Restam apenas " + produto.getEstoque() + " unidades."
                );
            }
        }

        Pedido pedido = new Pedido();
        pedido.setCodigoRastreio(gerarCodigoRastreio());
        pedido.setUser(userLogado);
        pedido.setNomeCliente(req.getNomeCliente().trim());
        pedido.setEmailCliente(req.getEmailCliente().trim().toLowerCase());
        pedido.setCpfCliente(req.getCpfCliente() != null ? req.getCpfCliente().replaceAll("\\D", "") : "");
        pedido.setTelefoneCliente(req.getTelefoneCliente() != null ? req.getTelefoneCliente().trim() : "");
        pedido.setEnderecoLogradouro(req.getEnderecoLogradouro().trim());
        pedido.setEnderecoNumero(req.getEnderecoNumero().trim());
        pedido.setEnderecoBairro(req.getEnderecoBairro() != null ? req.getEnderecoBairro().trim() : "");
        pedido.setEnderecoCidade(req.getEnderecoCidade().trim());
        pedido.setEnderecoUf(req.getEnderecoUf().trim().toUpperCase());
        pedido.setEnderecoCep(req.getEnderecoCep().replaceAll("\\D", ""));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setFormaPagamento(normalizarFormaPagamento(req.getFormaPagamento()));
        pedido.setSubtotal(resumo.subtotal());
        pedido.setFrete(resumo.frete());
        pedido.setDesconto(resumo.desconto());
        pedido.setTotal(resumo.total());

        // Snapshot dos itens; a baixa do estoque aguarda a confirmacao de pagamento.
        for (CarrinhoItem itemCarrinho : itensCarrinho) {
            Produto produto = itemCarrinho.getProduto();
            
            produto = produtoRepository.findById(produto.getId()).orElse(produto);

            ItemPedido itemPedido = new ItemPedido(
                produto,
                produto.getNome(),
                produto.getImagemUrl(),
                produto.getPreco(),
                itemCarrinho.getQuantidade()
            );
            pedido.adicionarItem(itemPedido);
        }

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Esvaziar carrinho
        carrinhoService.limparCarrinho(session);

        return toPedidoResponse(pedidoSalvo);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMeusPedidos(HttpSession session) {
        User user = authSessionService.currentUser(session);
        if (user == null || !authSessionService.isCliente(session)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Entre na sua conta para consultar seus pedidos.");
        }
        return pedidoRepository.findByUserIdOrderByDataCriacaoDesc(user.getId()).stream()
            .map(this::toPedidoResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorCodigoRastreio(String codigo, HttpSession session) {
        Pedido pedido = pedidoRepository.findByCodigoRastreio(codigo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        User user = authSessionService.currentUser(session);
        boolean owner = user != null && pedido.getUser() != null && pedido.getUser().getId().equals(user.getId());
        if (!owner && !authSessionService.isAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.");
        }
        return toPedidoResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodosAdmin() {
        return pedidoRepository.findAllByOrderByDataCriacaoDesc(org.springframework.data.domain.PageRequest.of(0, 100))
            .getContent().stream()
            .map(this::toPedidoResponse)
            .toList();
    }

    @Transactional
    public PedidoResponse atualizarStatusAdmin(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        pedido.setStatus(novoStatus);
        return toPedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public BigDecimal obterFaturamentoTotal() {
        return pedidoRepository.somarFaturamentoTotal();
    }

    @Transactional(readOnly = true)
    public long contarPedidosPorStatus(StatusPedido status) {
        return pedidoRepository.countByStatus(status);
    }

    private void validarDadosCheckout(FinalizarPedidoRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados de checkout são obrigatórios.");
        }
        if (isVazio(req.getNomeCliente())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome completo.");
        }
        if (isVazio(req.getEmailCliente())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um email válido.");
        }
        if (isVazio(req.getEnderecoLogradouro()) || isVazio(req.getEnderecoNumero()) || isVazio(req.getEnderecoCidade()) || isVazio(req.getEnderecoCep())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha todos os campos do endereço de entrega.");
        }
    }

    private boolean isVazio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String normalizarFormaPagamento(String forma) {
        if (forma == null) return "Cartão de Crédito";
        return switch (forma.toUpperCase()) {
            case "PIX" -> "Pix";
            case "DEBITO" -> "Débito Online";
            default -> "Cartão de Crédito";
        };
    }

    private String gerarCodigoRastreio() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = ByteBuffer.allocate(16)
            .putLong(uuid.getMostSignificantBits())
            .putLong(uuid.getLeastSignificantBits())
            .array();
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "DA-" + codigo;
    }

    public PedidoResponse toPedidoResponse(Pedido p) {
        String enderecoCompleto = String.format("%s, %s - %s. %s/%s - CEP: %s",
            p.getEnderecoLogradouro(),
            p.getEnderecoNumero(),
            p.getEnderecoBairro() != null ? p.getEnderecoBairro() : "",
            p.getEnderecoCidade(),
            p.getEnderecoUf(),
            p.getEnderecoCep()
        );

        List<PedidoResponse.ItemPedidoResponse> itensResponse = p.getItens().stream().map(item -> new PedidoResponse.ItemPedidoResponse(
            item.getId(),
            item.getProduto() != null ? item.getProduto().getId() : null,
            item.getNomeProduto(),
            item.getImagemUrl(),
            item.getPrecoUnitario(),
            currencyFormatter.format(item.getPrecoUnitario()),
            item.getQuantidade(),
            item.getSubtotal(),
            currencyFormatter.format(item.getSubtotal())
        )).toList();

        return new PedidoResponse(
            p.getId(),
            p.getCodigoRastreio(),
            p.getNomeCliente(),
            p.getEmailCliente(),
            p.getCpfCliente(),
            p.getTelefoneCliente(),
            enderecoCompleto,
            p.getDataCriacao(),
            p.getDataCriacao() != null ? p.getDataCriacao().format(dateFormatter) : "",
            p.getStatus().name(),
            p.getStatus().getDescricao(),
            p.getFormaPagamento(),
            p.getSubtotal(),
            currencyFormatter.format(p.getSubtotal()),
            p.getFrete(),
            p.getFrete().compareTo(BigDecimal.ZERO) == 0 ? "Grátis" : currencyFormatter.format(p.getFrete()),
            p.getDesconto(),
            currencyFormatter.format(p.getDesconto()),
            p.getTotal(),
            currencyFormatter.format(p.getTotal()),
            itensResponse
        );
    }
}
