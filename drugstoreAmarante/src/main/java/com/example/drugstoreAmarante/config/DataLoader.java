package com.example.drugstoreAmarante.config;

import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

//    @Bean
//    CommandLineRunner initDatabase(UserService userService) {
//        return args -> {
//            User user = new User();
//            user.setEmail("admin");
//            user.setPassword("123");
//
//            userService.salvar(user);
//        };
//    }
}