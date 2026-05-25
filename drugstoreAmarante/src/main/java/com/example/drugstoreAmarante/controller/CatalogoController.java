package com.example.drugstoreAmarante.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.drugstoreAmarante.dto.CategoriaResponse;
import com.example.drugstoreAmarante.dto.MarcaResponse;
import com.example.drugstoreAmarante.repository.CategoriaRepository;
import com.example.drugstoreAmarante.repository.MarcaRepository;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    public CatalogoController(CategoriaRepository categoriaRepository, MarcaRepository marcaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
    }

    @GetMapping("/categorias")
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
            .map(categoria -> new CategoriaResponse(categoria.getId(), categoria.getNome()))
            .toList();
    }

    @GetMapping("/marcas")
    public List<MarcaResponse> listarMarcas() {
        return marcaRepository.findAll().stream()
            .map(marca -> new MarcaResponse(marca.getId(), marca.getNome()))
            .toList();
    }
}
