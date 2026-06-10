package com.shadownet.service;

import com.shadownet.Repository.UserRepository;
import com.shadownet.dto.AuthResponse;
import com.shadownet.dto.LoginRequest;
import com.shadownet.dto.RegisterRequest;
import com.shadownet.model.User;
import com.shadownet.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BruteForceDetectionService bruteForceDetectionService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_ANALYST");

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            bruteForceDetectionService.recordFailedAttempt(ipAddress);
            throw new RuntimeException("Invalid credentials");
        }

        bruteForceDetectionService.recordSuccessfulAttempt(ipAddress);
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
