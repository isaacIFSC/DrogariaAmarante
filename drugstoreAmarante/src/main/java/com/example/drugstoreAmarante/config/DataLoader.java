package com.example.drugstoreAmarante.config;

import com.example.drugstoreAmarante.domain.PerfilUsuario;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner initDatabase(UserService userService) {
        return args -> {
            String email = env("DROGARIA_ADMIN_EMAIL");
            String password = env("DROGARIA_ADMIN_PASSWORD");
            String cpf = env("DROGARIA_ADMIN_CPF");
            String phone = env("DROGARIA_ADMIN_PHONE");
            if (email == null || password == null || cpf == null || phone == null) {
                log.warn("Administrador inicial nao criado: defina DROGARIA_ADMIN_EMAIL, DROGARIA_ADMIN_PASSWORD, DROGARIA_ADMIN_CPF e DROGARIA_ADMIN_PHONE.");
                return;
            }

            User existingAdmin = userService.findByEmail(email);
            if (existingAdmin != null) {
                if (existingAdmin.getPerfil() == null) {
                    existingAdmin.setPerfil(PerfilUsuario.ADMIN);
                    userService.salvarAdmin(existingAdmin);
                }
                return;
            }

            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Amarante");
            admin.setEmail(email);
            admin.setPassword(password);
            admin.setCpf(cpf);
            admin.setPhone(phone);
            admin.setGender("Nao informado");
            admin.setPerfil(PerfilUsuario.ADMIN);

            userService.salvarAdmin(admin);
        };
    }

    private String env(String name) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
