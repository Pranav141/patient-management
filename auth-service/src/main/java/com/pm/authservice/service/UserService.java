package com.pm.authservice.service;

import com.pm.authservice.exception.EmailAlreadyRegisteredException;
import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User findByEmail(String email){
        User u = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email not Found"));
        return u;
    }

    public Boolean register(String email, String password, String role) {
        if(userRepo.existsByEmail(email)){
            throw new EmailAlreadyRegisteredException("The email is already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        try {
            userRepo.save(user);
            return true;

        } catch (RuntimeException e) {
            return false;
        }
    }
}
