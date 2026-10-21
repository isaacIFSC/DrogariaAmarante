package com.example.drugstoreAmarante.config;

import com.example.drugstoreAmarante.domain.PerfilUsuario;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.AuthSessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.web.csrf.CsrfToken;

@ControllerAdvice
public class SessionModelAdvice {

    @Autowired
    private AuthSessionService authSessionService;

    @ModelAttribute("usuarioLogado")
    public User usuarioLogado(HttpSession session) {
        return authSessionService.currentUser(session);
    }

    @ModelAttribute("clienteLogado")
    public boolean clienteLogado(HttpSession session) {
        User usuario = authSessionService.currentUser(session);
        return usuario != null && usuario.getPerfil() == PerfilUsuario.CLIENTE;
    }

    @ModelAttribute("adminLogado")
    public boolean adminLogado(HttpSession session) {
        User usuario = authSessionService.currentUser(session);
        return usuario != null && usuario.getPerfil() == PerfilUsuario.ADMIN;
    }

    @ModelAttribute("csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}
