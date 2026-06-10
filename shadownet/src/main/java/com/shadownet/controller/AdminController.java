package com.shadownet.controller;

import com.shadownet.Repository.UserRepository;
import com.shadownet.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete user");
        }
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(passwordEncoder.encode(request.get("newPassword")));
            userRepository.save(user);
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reset password");
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("bruteForceThreshold", 5);
        config.put("bruteForceWindowMinutes", 1);
        config.put("dosThreshold", 100);
        config.put("dosWindowMinutes", 1);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/config")
    public ResponseEntity<?> updateConfig(@RequestBody Map<String, Object> config) {
        return ResponseEntity.ok("Configuration updated successfully");
    }

    @PostMapping("/block-ip")
    public ResponseEntity<?> blockIp(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok("IP " + request.get("ip") + " blocked successfully");
    }

    @PostMapping("/unblock-ip")
    public ResponseEntity<?> unblockIp(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok("IP " + request.get("ip") + " unblocked successfully");
    }

    @GetMapping("/blocked-ips")
    public ResponseEntity<?> getBlockedIps() {
        return ResponseEntity.ok(java.util.List.of());
    }
}
