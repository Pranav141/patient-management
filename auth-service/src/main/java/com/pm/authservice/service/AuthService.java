package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepo;
import com.pm.authservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(LoginRequestDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();
        User user = userService.findByEmail(email);
        if(passwordEncoder.matches(password, user.getPassword())){
            //generate token
            return jwtUtil.generateToken(user.getEmail(),user.getRole());
        }
        else{
            throw new BadCredentialsException("Wrong Password");
        }
    }

    public String register(RegisterRequestDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String role = request.getRole();
        String hashedPassword = passwordEncoder.encode(password);
        Boolean registered = userService.register(email,hashedPassword,role);
        if(registered){
            return "User Created Successfully";
        }
        return "User creation Failed";
    }

    public void validateToken(String token){
        jwtUtil.validateToken(token);
    }
}
