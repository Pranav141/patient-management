package com.pm.authservice.controller;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.exception.AuthorizationHeaderMissingException;
import com.pm.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login Route that generates a JWT token based on Email and password")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        String token = authService.login(request);
        return ResponseEntity
                .status(HttpStatusCode.valueOf(200))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .body(new LoginResponseDTO(token));

    }

    @Operation(summary = "Register route")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request){
        String response = authService.register(request);
        return  ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Validate Token Endpoint")
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader(value = "Authorization") String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new AuthorizationHeaderMissingException("Didn't Find Authorization Header");
        }
        authService.validateToken(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
}
