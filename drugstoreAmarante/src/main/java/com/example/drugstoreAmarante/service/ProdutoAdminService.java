package com.example.drugstoreAmarante.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.drugstoreAmarante.domain.Categoria;
import com.example.drugstoreAmarante.domain.Marca;
import com.example.drugstoreAmarante.domain.Produto;
import com.example.drugstoreAmarante.dto.ProdutoCadastroRequest;
import com.example.drugstoreAmarante.dto.ProdutoImportacaoResposta;
import com.example.drugstoreAmarante.dto.ProdutoResponse;
import com.example.drugstoreAmarante.repository.CategoriaRepository;
import com.example.drugstoreAmarante.repository.MarcaRepository;
import com.example.drugstoreAmarante.repository.ProdutoRepository;

@Service
public class ProdutoAdminService {

    private static final String IMAGEM_PADRAO = "/images/produto-placeholder.svg";

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    public ProdutoAdminService(
        ProdutoRepository produtoRepository,
        CategoriaRepository categoriaRepository,
        MarcaRepository marcaRepository
    ) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
    }

    @Transactional
    public ProdutoResponse cadastrar(ProdutoCadastroRequest request) {
        validarRequest(request);

        Produto produto = new Produto(
            limparTexto(request.nome()),
            limparTexto(request.descricao()),
            request.preco(),
            resolverImagemUrl(request.imagemUrl()),
            request.estoque(),
            resolverCategoria(request.categoria()),
            resolverMarca(request.marca())
        );

        Produto salvo = produtoRepository.save(produto);
        return toResponse(salvo);
    }

    @Transactional
    public ProdutoImportacaoResposta importarCsv(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo CSV valido");
        }

        int linhasLidas = 0;
        int produtosCriados = 0;
        List<String> erros = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O CSV precisa conter um cabecalho");
            }

            char delimiter = descobrirDelimitador(headerLine);
            List<String> cabecalho = parseCsvLine(headerLine, delimiter);
            Map<String, Integer> colunas = mapearCabecalho(cabecalho);
            validarCabecalho(colunas);

            String line;
            int numeroLinha = 1;
            while ((line = reader.readLine()) != null) {
                numeroLinha += 1;
                if (line.isBlank()) {
                    continue;
                }

                linhasLidas += 1;
                try {
                    List<String> campos = parseCsvLine(line, delimiter);
                    ProdutoCadastroRequest request = new ProdutoCadastroRequest(
                        valorCampo(campos, colunas, "nome"),
                        valorCampo(campos, colunas, "descricao"),
                        parsePreco(valorCampo(campos, colunas, "preco")),
                        valorCampo(campos, colunas, "imagemurl"),
                        parseInteiro(valorCampo(campos, colunas, "estoque")),
                        valorCampo(campos, colunas, "categoria"),
                        valorCampo(campos, colunas, "marca")
                    );
                    cadastrar(request);
                    produtosCriados += 1;
                } catch (RuntimeException ex) {
                    erros.add("Linha " + numeroLinha + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao foi possivel ler o arquivo CSV", ex);
        }

        return new ProdutoImportacaoResposta(linhasLidas, produtosCriados, erros);
    }

    public List<ProdutoResponse> listarRecentes() {
        return produtoRepository.findAll().stream()
            .sorted((a, b) -> b.getId().compareTo(a.getId()))
            .limit(12)
            .map(this::toResponse)
            .toList();
    }

    private ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.getImagemUrl(),
            produto.getEstoque(),
            produto.isAtivo() && produto.getEstoque() != null && produto.getEstoque() > 0,
            produto.getCategoria().getNome(),
            produto.getMarca().getNome()
        );
    }

    private void validarRequest(ProdutoCadastroRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do produto sao obrigatorios");
        }
        if (estaVazio(request.nome())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do produto");
        }
        if (estaVazio(request.descricao())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a descricao do produto");
        }
        if (request.preco() == null || request.preco().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um preco valido");
        }
        if (request.estoque() == null || request.estoque() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um estoque valido");
        }
        if (estaVazio(request.categoria())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a categoria do produto");
        }
        if (estaVazio(request.marca())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a marca do produto");
        }
    }

    private Categoria resolverCategoria(String nome) {
        String valor = limparTexto(nome);
        return categoriaRepository.findByNomeIgnoreCase(valor)
            .orElseGet(() -> categoriaRepository.save(new Categoria(valor)));
    }

    private Marca resolverMarca(String nome) {
        String valor = limparTexto(nome);
        return marcaRepository.findByNomeIgnoreCase(valor)
            .orElseGet(() -> marcaRepository.save(new Marca(valor)));
    }

    private String resolverImagemUrl(String imagemUrl) {
        if (estaVazio(imagemUrl)) {
            return IMAGEM_PADRAO;
        }
        return limparTexto(imagemUrl);
    }

    private String limparTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private boolean estaVazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private BigDecimal parsePreco(String valor) {
        if (estaVazio(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preco ausente");
        }

        try {
            return new BigDecimal(valor.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preco invalido: " + valor, ex);
        }
    }

    private Integer parseInteiro(String valor) {
        if (estaVazio(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque ausente");
        }

        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque invalido: " + valor, ex);
        }
    }

    private char descobrirDelimitador(String linha) {
        long pontoEVirgula = linha.chars().filter(ch -> ch == ';').count();
        long virgula = linha.chars().filter(ch -> ch == ',').count();
        return pontoEVirgula > virgula ? ';' : ',';
    }

    private Map<String, Integer> mapearCabecalho(List<String> cabecalho) {
        Map<String, Integer> colunas = new HashMap<>();
        for (int i = 0; i < cabecalho.size(); i += 1) {
            colunas.put(normalizarNomeColuna(cabecalho.get(i)), i);
        }
        return colunas;
    }

    private void validarCabecalho(Map<String, Integer> colunas) {
        List<String> obrigatorias = List.of("nome", "descricao", "preco", "estoque", "categoria", "marca");
        for (String coluna : obrigatorias) {
            if (!colunas.containsKey(coluna)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cabecalho CSV precisa conter a coluna: " + coluna);
            }
        }
    }

    private String valorCampo(List<String> campos, Map<String, Integer> colunas, String coluna) {
        Integer indice = colunas.get(coluna);
        if (indice == null || indice < 0 || indice >= campos.size()) {
            return null;
        }
        return campos.get(indice);
    }

    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean emAspas = false;

        for (int i = 0; i < line.length(); i += 1) {
            char caractere = line.charAt(i);
            if (caractere == '"') {
                if (emAspas && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    atual.append('"');
                    i += 1;
                } else {
                    emAspas = !emAspas;
                }
                continue;
            }

            if (caractere == delimiter && !emAspas) {
                result.add(atual.toString().trim());
                atual.setLength(0);
                continue;
            }

            atual.append(caractere);
        }

        result.add(atual.toString().trim());
        return result;
    }

    private String normalizarNomeColuna(String valor) {
        String semAcentos = Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
