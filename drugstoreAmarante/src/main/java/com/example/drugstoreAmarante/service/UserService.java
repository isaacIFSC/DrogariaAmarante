package com.example.drugstoreAmarante.service;

import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void salvar(User user){
        userRepository.save(user);
    }
}
