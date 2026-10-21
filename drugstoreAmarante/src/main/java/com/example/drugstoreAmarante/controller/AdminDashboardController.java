package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.domain.StatusPedido;
import com.example.drugstoreAmarante.service.AuthSessionService;
import com.example.drugstoreAmarante.service.PedidoService;
import com.example.drugstoreAmarante.service.ProdutoAdminService;
import com.example.drugstoreAmarante.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.NumberFormat;
import java.util.Locale;

@Controller
public class AdminDashboardController {

    private final AuthSessionService authSessionService;
    private final ProdutoAdminService produtoAdminService;
    private final UserService userService;
    private final PedidoService pedidoService;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public AdminDashboardController(
        AuthSessionService authSessionService,
        ProdutoAdminService produtoAdminService,
        UserService userService,
        PedidoService pedidoService
    ) {
        this.authSessionService = authSessionService;
        this.produtoAdminService = produtoAdminService;
        this.userService = userService;
        this.pedidoService = pedidoService;
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        if (!authSessionService.isAdmin(session)) {
            return "redirect:/admin/login?next=/admin/dashboard";
        }

        model.addAttribute("totalProdutos", produtoAdminService.countProdutos());
        model.addAttribute("produtosAtivos", produtoAdminService.countProdutosAtivos());
        model.addAttribute("totalClientes", userService.countClientes());
        model.addAttribute("totalAdmins", userService.countAdmins());
        model.addAttribute("faturamentoTotalFormatado", currencyFormatter.format(pedidoService.obterFaturamentoTotal()));
        model.addAttribute("pedidosPagosCount", pedidoService.contarPedidosPorStatus(StatusPedido.PAGO));
        model.addAttribute("ultimosProdutos", produtoAdminService.listarRecentes().stream().limit(4).toList());
        model.addAttribute("ultimosPedidos", pedidoService.listarTodosAdmin());
        return "admin/dashboard";
    }
}
