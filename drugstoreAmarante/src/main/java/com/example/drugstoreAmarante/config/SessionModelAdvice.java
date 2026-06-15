package com.example.drugstoreAmarante.config;

import com.example.drugstoreAmarante.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SessionModelAdvice {

    @ModelAttribute("usuarioLogado")
    public User usuarioLogado(HttpSession session) {
        Object usuario = session.getAttribute("usuarioLogado");
        return usuario instanceof User user ? user : null;
    }
}
