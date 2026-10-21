package com.example.drugstoreAmarante.service;

import com.example.drugstoreAmarante.domain.PerfilUsuario;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User authenticate(String login, PerfilUsuario perfil, String rawPassword) {
        User user = findByLoginAndPerfil(login, perfil);
        if (user == null || rawPassword == null || user.getPassword() == null) {
            return null;
        }

        String storedPassword = user.getPassword();
        boolean encoded = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$");
        boolean matches = encoded ? passwordEncoder.matches(rawPassword, storedPassword) : storedPassword.equals(rawPassword);
        if (!matches) {
            return null;
        }

        if (!encoded) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
        return user;
    }

    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return userRepository.findByEmail(normalizeEmail(email)).orElse(null);
    }

    public User findByLogin(String login) {
        if (login == null || login.isBlank()) {
            return null;
        }

        String value = login.trim();
        if (value.contains("@")) {
            return userRepository.findByEmail(normalizeEmail(value)).orElse(null);
        }

        String cpf = value.replaceAll("\\D", "");
        return userRepository.findByCpf(cpf).orElse(null);
    }

    public boolean existsByCpf(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);
        return !normalizedCpf.isBlank() && userRepository.findByCpf(normalizedCpf).isPresent();
    }

    public boolean existsByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return !normalizedEmail.isBlank() && userRepository.findByEmail(normalizedEmail).isPresent();
    }

    public long countClientes() {
        return userRepository.countByPerfil(PerfilUsuario.CLIENTE);
    }

    public long countAdmins() {
        return userRepository.countByPerfil(PerfilUsuario.ADMIN);
    }

    public User findByLoginAndPerfil(String login, PerfilUsuario perfil) {
        User user = findByLogin(login);
        if (user == null || perfil == null) {
            return null;
        }

        if (user.getPerfil() == null) {
            return perfil == PerfilUsuario.CLIENTE ? user : null;
        }

        if (user.getPerfil() != perfil) {
            return null;
        }

        return user;
    }

    public User salvarCliente(User user) {
        normalizeCadastro(user);
        user.setPerfil(PerfilUsuario.CLIENTE);
        hashPasswordIfNeeded(user);
        return userRepository.save(user);
    }

    public User salvarAdmin(User user) {
        normalizeCadastro(user);
        user.setPerfil(PerfilUsuario.ADMIN);
        hashPasswordIfNeeded(user);
        return userRepository.save(user);
    }

    public User salvar(User user){
        hashPasswordIfNeeded(user);
        return userRepository.save(user);
    }

    private void normalizeCadastro(User user) {
        if (user == null) {
            return;
        }

        user.setCpf(normalizeCpf(user.getCpf()));
        user.setEmail(normalizeEmail(user.getEmail()));
        user.setFirstName(trimToNull(user.getFirstName()));
        user.setLastName(trimToNull(user.getLastName()));
        user.setGender(trimToNull(user.getGender()));
        user.setPhone(trimToNull(user.getPhone()));
    }

    private void hashPasswordIfNeeded(User user) {
        if (user != null && user.getPassword() != null && !user.getPassword().isBlank()) {
            String password = user.getPassword();
            if (!(password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"))) {
                user.setPassword(passwordEncoder.encode(password));
            }
        }
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
