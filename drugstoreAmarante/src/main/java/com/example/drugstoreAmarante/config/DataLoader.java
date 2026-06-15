package com.example.drugstoreAmarante.config;

import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UserService userService) {
        return args -> {
            if (userService.findByEmail("admin@drugstoreamarante.com") != null) {
                return;
            }

            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Amarante");
            admin.setEmail("admin@drugstoreamarante.com");
            admin.setPassword("123456");
            admin.setCpf("00000000000");
            admin.setPhone("11999999999");
            admin.setGender("Nao informado");

            userService.salvar(admin);
        };
    }
}
