package com.example.drugstoreAmarante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.drugstoreAmarante.domain.Produto;
import com.example.drugstoreAmarante.repository.ProdutoRepository;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProdutoControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void deveListarProdutosPorCategoriaComOrdenacao() throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder(URI.create(url("/api/produtos?categoria=Higiene&ordenacao=preco_asc"))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);

        Object primeiroNome = JsonPath.read(response.body(), "$[0].nome");
        Object segundoNome = JsonPath.read(response.body(), "$[1].nome");

        assertEquals("Escova Dental Colgate Triple Action", primeiroNome);
        assertEquals("Creme Dental Colgate Total 12", segundoNome);
    }

    @Test
    void deveRetornarDetalheDoProdutoComCategoriaEMarca() throws Exception {
        Produto produto = produtoRepository.findAll().stream()
            .filter(item -> item.getNome().equals("Monitor de Pressao Omron HEM-7122"))
            .findFirst()
            .orElseThrow();

        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder(URI.create(url("/api/produtos/" + produto.getId()))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
        assertEquals("Monitor de Pressao Omron HEM-7122", JsonPath.read(response.body(), "$.nome"));
        assertEquals("Saude", JsonPath.read(response.body(), "$.categoria"));
        assertEquals("Omron", JsonPath.read(response.body(), "$.marca"));
        assertTrue(JsonPath.read(response.body(), "$.descricao") != null);
    }

    @Test
    void deveExporCategoriasEMarcasDoCatalogo() throws Exception {
        HttpResponse<String> categorias = httpClient.send(
            HttpRequest.newBuilder(URI.create(url("/api/catalogo/categorias"))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> marcas = httpClient.send(
            HttpRequest.newBuilder(URI.create(url("/api/catalogo/marcas"))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(categorias.statusCode() >= 200 && categorias.statusCode() < 300);
        assertTrue(marcas.statusCode() >= 200 && marcas.statusCode() < 300);
        assertEquals(3, ((Number) JsonPath.read(categorias.body(), "$.length()")).intValue());
        assertEquals(3, ((Number) JsonPath.read(marcas.body(), "$.length()")).intValue());
    }

    @Test
    void devePaginarCatalogoComFiltros() throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder(URI.create(
                url("/api/catalogo/produtos?categoria=Higiene&minPreco=10&tamanho=1&pagina=0")
            )).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertEquals(1, ((Number) JsonPath.read(response.body(), "$.produtos.length()")).intValue());
        assertEquals("Creme Dental Colgate Total 12", JsonPath.read(response.body(), "$.produtos[0].nome"));
        assertEquals(1, ((Number) JsonPath.read(response.body(), "$.totalProdutos")).intValue());
        assertEquals(false, JsonPath.read(response.body(), "$.temProximaPagina"));
    }

    @Test
    void deveRejeitarFaixaDePrecoInvalidaNoCatalogo() throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder(URI.create(url("/api/catalogo/produtos?minPreco=30&maxPreco=10"))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(400, response.statusCode());
        assertEquals(
            "O preco minimo nao pode ser maior que o preco maximo",
            JsonPath.read(response.body(), "$.message")
        );
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
