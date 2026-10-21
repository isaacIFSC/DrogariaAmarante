package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.domain.StatusPedido;
import com.example.drugstoreAmarante.service.AuthSessionService;
import com.example.drugstoreAmarante.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminPedidoController {

    private final AuthSessionService authSessionService;
    private final PedidoService pedidoService;

    public AdminPedidoController(AuthSessionService authSessionService, PedidoService pedidoService) {
        this.authSessionService = authSessionService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/admin/pedidos")
    public String pedidos(@RequestParam(required = false) StatusPedido status, HttpSession session, Model model) {
        if (!authSessionService.isAdmin(session)) {
            return "redirect:/admin/login?next=/admin/pedidos";
        }
        var pedidos = pedidoService.listarTodosAdmin();
        model.addAttribute("pedidos", status == null ? pedidos : pedidos.stream().filter(p -> p.status().equals(status.name())).toList());
        model.addAttribute("statusPedidos", StatusPedido.values());
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("totalPedidos", pedidos.size());
        model.addAttribute("adminPage", true);
        return "admin/pedidos";
    }

    @PostMapping("/admin/pedidos/status")
    public String atualizarStatus(@RequestParam Long pedidoId, @RequestParam StatusPedido status,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        if (!authSessionService.isAdmin(session)) {
            return "redirect:/admin/login?next=/admin/pedidos";
        }
        pedidoService.atualizarStatusAdmin(pedidoId, status);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Status do pedido atualizado com sucesso.");
        return "redirect:/admin/pedidos";
    }
}
