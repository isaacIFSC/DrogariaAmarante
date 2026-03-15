package com.example.drugstoreAmarante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
public class DrugstoreAmaranteApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrugstoreAmaranteApplication.class, args);
    }

}
