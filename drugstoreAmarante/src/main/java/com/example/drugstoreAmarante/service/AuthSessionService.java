package com.example.drugstoreAmarante.service;

import com.example.drugstoreAmarante.domain.PerfilUsuario;
import com.example.drugstoreAmarante.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    public static final String SESSION_USER = "usuarioLogado";

    public User currentUser(HttpSession session) {
        Object value = session.getAttribute(SESSION_USER);
        return value instanceof User user ? user : null;
    }

    public void login(HttpSession session, User user) {
        session.setAttribute(SESSION_USER, safeSessionUser(user));
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public boolean isCliente(HttpSession session) {
        User user = currentUser(session);
        return user != null && (user.getPerfil() == null || user.getPerfil() == PerfilUsuario.CLIENTE);
    }

    public boolean isAdmin(HttpSession session) {
        User user = currentUser(session);
        return user != null && user.getPerfil() == PerfilUsuario.ADMIN;
    }

    private User safeSessionUser(User source) {
        User safe = new User();
        safe.setId(source.getId());
        safe.setFirstName(source.getFirstName());
        safe.setLastName(source.getLastName());
        safe.setEmail(source.getEmail());
        safe.setCpf(source.getCpf());
        safe.setPhone(source.getPhone());
        safe.setGender(source.getGender());
        safe.setBirthDate(source.getBirthDate());
        safe.setPerfil(source.getPerfil());
        return safe;
    }
}
