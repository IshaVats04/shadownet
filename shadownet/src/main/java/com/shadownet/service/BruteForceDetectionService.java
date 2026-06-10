package com.shadownet.service;

import com.shadownet.Repository.AlertRepository;
import com.shadownet.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceDetectionService {

    @Autowired
    private AlertRepository alertRepository;

    private final ConcurrentHashMap<String, List<Long>> failedAttempts = new ConcurrentHashMap<>();
    private final int MAX_ATTEMPTS = 5;
    private final long TIME_WINDOW_MS = 60000; // 1 minute

    public void recordFailedAttempt(String ipAddress) {
        long currentTime = System.currentTimeMillis();
        failedAttempts.computeIfAbsent(ipAddress, k -> new ArrayList<>()).add(currentTime);
        
        cleanupOldAttempts(ipAddress, currentTime);
        
        List<Long> attempts = failedAttempts.get(ipAddress);
        if (attempts.size() >= MAX_ATTEMPTS) {
            generateAlert(ipAddress, "BRUTE_FORCE", "HIGH");
            failedAttempts.remove(ipAddress); // Reset after alert
        }
    }

    public void recordSuccessfulAttempt(String ipAddress) {
        failedAttempts.remove(ipAddress); // Clear on successful login
    }

    private void cleanupOldAttempts(String ipAddress, long currentTime) {
        List<Long> attempts = failedAttempts.get(ipAddress);
        if (attempts != null) {
            attempts.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW_MS);
        }
    }

    private void generateAlert(String ipAddress, String attackType, String severity) {
        Alert alert = new Alert();
        alert.setIpAddress(ipAddress);
        alert.setAttackType(attackType);
        alert.setSeverity(severity);
        alert.setThreatScore(90); // HIGH = 90
        alert.setTimestamp(java.time.LocalDateTime.now());
        alertRepository.save(alert);
    }
}
