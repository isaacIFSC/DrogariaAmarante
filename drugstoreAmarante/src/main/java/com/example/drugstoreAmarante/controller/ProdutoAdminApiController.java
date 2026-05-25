package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.ProdutoCadastroRequest;
import com.example.drugstoreAmarante.dto.ProdutoImportacaoResposta;
import com.example.drugstoreAmarante.dto.ProdutoResponse;
import com.example.drugstoreAmarante.service.ProdutoAdminService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admin/produtos")
public class ProdutoAdminApiController {

    private final ProdutoAdminService produtoAdminService;

    public ProdutoAdminApiController(ProdutoAdminService produtoAdminService) {
        this.produtoAdminService = produtoAdminService;
    }

    @PostMapping
    public ProdutoResponse cadastrar(@RequestBody ProdutoCadastroRequest request) {
        return produtoAdminService.cadastrar(request);
    }

    @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProdutoImportacaoResposta importarCsv(@RequestParam("arquivo") MultipartFile arquivo) {
        return produtoAdminService.importarCsv(arquivo);
    }

    @GetMapping("/recentes")
    public List<ProdutoResponse> listarRecentes() {
        return produtoAdminService.listarRecentes();
    }
}
