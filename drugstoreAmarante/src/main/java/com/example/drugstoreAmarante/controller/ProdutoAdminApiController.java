package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.ProdutoCadastroRequest;
import com.example.drugstoreAmarante.dto.ProdutoImportacaoResposta;
import com.example.drugstoreAmarante.dto.ProdutoResponse;
import com.example.drugstoreAmarante.service.AuthSessionService;
import com.example.drugstoreAmarante.service.ProdutoAdminService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/produtos")
public class ProdutoAdminApiController {

    private final ProdutoAdminService produtoAdminService;
    private final AuthSessionService authSessionService;

    public ProdutoAdminApiController(ProdutoAdminService produtoAdminService, AuthSessionService authSessionService) {
        this.produtoAdminService = produtoAdminService;
        this.authSessionService = authSessionService;
    }

    @PostMapping
    public ProdutoResponse cadastrar(@RequestBody ProdutoCadastroRequest request, HttpSession session) {
        exigirAdmin(session);
        return produtoAdminService.cadastrar(request);
    }

    @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProdutoImportacaoResposta importarCsv(@RequestParam("arquivo") MultipartFile arquivo, HttpSession session) {
        exigirAdmin(session);
        return produtoAdminService.importarCsv(arquivo);
    }

    @GetMapping("/recentes")
    public List<ProdutoResponse> listarRecentes(HttpSession session) {
        exigirAdmin(session);
        return produtoAdminService.listarRecentes();
    }

    private void exigirAdmin(HttpSession session) {
        if (!authSessionService.isAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso restrito. Faça login novamente.");
        }
    }
}
