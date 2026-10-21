package com.example.drugstoreAmarante.service;

import com.example.drugstoreAmarante.domain.CarrinhoItem;
import com.example.drugstoreAmarante.domain.Produto;
import com.example.drugstoreAmarante.dto.CarrinhoItemResponse;
import com.example.drugstoreAmarante.dto.CarrinhoResumoResponse;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.repository.CarrinhoItemRepository;
import com.example.drugstoreAmarante.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CarrinhoService {

    public static final String SESSION_KEY_ATTR = "DROGARIA_AMARANTE_CART_SESSION_KEY";

    private final CarrinhoItemRepository carrinhoItemRepository;
    private final ProdutoRepository produtoRepository;
    private final AuthSessionService authSessionService;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public CarrinhoService(
        CarrinhoItemRepository carrinhoItemRepository,
        ProdutoRepository produtoRepository,
        AuthSessionService authSessionService
    ) {
        this.carrinhoItemRepository = carrinhoItemRepository;
        this.produtoRepository = produtoRepository;
        this.authSessionService = authSessionService;
    }

    public String getOrCreateSessionKey(HttpSession session) {
        String sessionKey = (String) session.getAttribute(SESSION_KEY_ATTR);
        if (sessionKey == null || sessionKey.isBlank()) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute(SESSION_KEY_ATTR, sessionKey);
        }
        return sessionKey;
    }

    @Transactional(readOnly = true)
    public CarrinhoResumoResponse obterResumo(HttpSession session) {
        List<CarrinhoItem> itens = obterItens(session);

        List<CarrinhoItemResponse> itensResponse = itens.stream()
            .filter(i -> i != null && i.getProduto() != null)
            .map(this::toItemResponse)
            .toList();

        int quantidadeTotal = itensResponse.stream().mapToInt(CarrinhoItemResponse::quantidade).sum();

        BigDecimal subtotal = itensResponse.stream()
            .map(CarrinhoItemResponse::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal frete = subtotal.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : (subtotal.compareTo(new BigDecimal("150.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("12.90"));

        BigDecimal desconto = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(frete).subtract(desconto);

        return new CarrinhoResumoResponse(
            itensResponse,
            quantidadeTotal,
            subtotal,
            currencyFormatter.format(subtotal),
            frete,
            frete.compareTo(BigDecimal.ZERO) == 0 ? "Grátis" : currencyFormatter.format(frete),
            desconto,
            currencyFormatter.format(desconto),
            total,
            currencyFormatter.format(total)
        );
    }

    @Transactional
    public CarrinhoResumoResponse adicionarItem(Long produtoId, Integer quantidade, HttpSession session) {
        if (produtoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido.");
        }
        int qtd = (quantidade == null || quantidade < 1) ? 1 : quantidade;

        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));

        if (!produto.isAtivo() || produto.getEstoque() == null || produto.getEstoque() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponível no estoque.");
        }

        User user = authSessionService.currentUser(session);
        String sessionKey = getOrCreateSessionKey(session);

        List<CarrinhoItem> itensExistentes = user != null
            ? carrinhoItemRepository.findByUserIdAndProdutoId(user.getId(), produtoId)
            : carrinhoItemRepository.findBySessionKeyAndProdutoId(sessionKey, produtoId);

        if (!itensExistentes.isEmpty()) {
            CarrinhoItem item = itensExistentes.get(0);
            int itemQtdAtual = (item.getQuantidade() != null) ? item.getQuantidade() : 0;
            int novaQtd = itemQtdAtual + qtd;
            if (novaQtd > produto.getEstoque()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque máximo atingido (" + produto.getEstoque() + " unidades).");
            }
            item.setQuantidade(novaQtd);
            carrinhoItemRepository.save(item);

            for (int i = 1; i < itensExistentes.size(); i++) {
                carrinhoItemRepository.delete(itensExistentes.get(i));
            }
        } else {
            if (qtd > produto.getEstoque()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque máximo atingido (" + produto.getEstoque() + " unidades).");
            }
            CarrinhoItem novo = new CarrinhoItem(sessionKey, user, produto, qtd);
            carrinhoItemRepository.save(novo);
        }

        return obterResumo(session);
    }

    @Transactional
    public CarrinhoResumoResponse atualizarQuantidade(Long itemId, Integer quantidade, HttpSession session) {
        if (itemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item inválido.");
        }

        CarrinhoItem item = findOwnedItem(itemId, session)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item do carrinho não encontrado."));

        if (quantidade == null || quantidade <= 0) {
            carrinhoItemRepository.delete(item);
        } else {
            if (item.getProduto() != null && item.getProduto().getEstoque() != null && quantidade > item.getProduto().getEstoque()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque insuficiente. Disponível: " + item.getProduto().getEstoque());
            }
            item.setQuantidade(quantidade);
            carrinhoItemRepository.save(item);
        }

        return obterResumo(session);
    }

    @Transactional
    public CarrinhoResumoResponse removerItem(Long itemId, HttpSession session) {
        if (itemId != null) {
            findOwnedItem(itemId, session).ifPresent(carrinhoItemRepository::delete);
        }
        return obterResumo(session);
    }

    @Transactional
    public void limparCarrinho(HttpSession session) {
        User user = authSessionService.currentUser(session);
        if (user != null) {
            carrinhoItemRepository.deleteByUserId(user.getId());
        } else {
            String sessionKey = getOrCreateSessionKey(session);
            carrinhoItemRepository.deleteBySessionKey(sessionKey);
        }
    }

    @Transactional
    public void fundirCarrinhoVisitanteSeHouver(HttpSession session, User user) {
        String sessionKey = (String) session.getAttribute(SESSION_KEY_ATTR);
        if (sessionKey == null || user == null) {
            return;
        }

        List<CarrinhoItem> itensVisitante = carrinhoItemRepository.findBySessionKey(sessionKey);
        for (CarrinhoItem itemVisitante : itensVisitante) {
            if (itemVisitante.getProduto() == null) continue;
            List<CarrinhoItem> itensUser = carrinhoItemRepository.findByUserIdAndProdutoId(user.getId(), itemVisitante.getProduto().getId());
            if (!itensUser.isEmpty()) {
                CarrinhoItem item = itensUser.get(0);
                int estoque = item.getProduto() != null && item.getProduto().getEstoque() != null ? item.getProduto().getEstoque() : 99;
                int qAtual = item.getQuantidade() != null ? item.getQuantidade() : 1;
                int qVisitante = itemVisitante.getQuantidade() != null ? itemVisitante.getQuantidade() : 1;
                item.setQuantidade(Math.min(qAtual + qVisitante, estoque));
                carrinhoItemRepository.save(item);
                carrinhoItemRepository.delete(itemVisitante);
            } else {
                itemVisitante.setUser(user);
                itemVisitante.setSessionKey(null);
                carrinhoItemRepository.save(itemVisitante);
            }
        }
    }

    public List<CarrinhoItem> obterItens(HttpSession session) {
        User user = authSessionService.currentUser(session);
        if (user != null) {
            return carrinhoItemRepository.findByUserId(user.getId());
        }
        String sessionKey = getOrCreateSessionKey(session);
        return carrinhoItemRepository.findBySessionKey(sessionKey);
    }

    private java.util.Optional<CarrinhoItem> findOwnedItem(Long itemId, HttpSession session) {
        User user = authSessionService.currentUser(session);
        if (user != null) {
            return carrinhoItemRepository.findByIdAndUserId(itemId, user.getId());
        }
        return carrinhoItemRepository.findByIdAndSessionKey(itemId, getOrCreateSessionKey(session));
    }

    private CarrinhoItemResponse toItemResponse(CarrinhoItem item) {
        Produto p = item.getProduto();
        BigDecimal preco = (p != null && p.getPreco() != null) ? p.getPreco() : BigDecimal.ZERO;
        int qtd = (item.getQuantidade() != null && item.getQuantidade() > 0) ? item.getQuantidade() : 1;
        BigDecimal total = preco.multiply(BigDecimal.valueOf(qtd));
        return new CarrinhoItemResponse(
            item.getId(),
            p != null ? p.getId() : null,
            p != null ? p.getNome() : "Produto",
            p != null ? p.getDescricao() : "",
            preco,
            currencyFormatter.format(preco),
            qtd,
            total,
            currencyFormatter.format(total),
            p != null ? p.getImagemUrl() : null,
            p != null ? p.getEstoque() : 0
        );
    }
}
