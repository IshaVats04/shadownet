package com.shadownet.service;

import com.shadownet.Repository.AlertRepository;
import com.shadownet.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public Alert generateAlert(String ipAddress, String attackType, String severity, int threatScore) {
        Alert alert = new Alert();
        alert.setIpAddress(ipAddress);
        alert.setAttackType(attackType);
        alert.setSeverity(severity);
        alert.setThreatScore(threatScore);
        alert.setTimestamp(java.time.LocalDateTime.now());
        return alertRepository.save(alert);
    }

    public Alert generateAlert(String ipAddress, String attackType, String severity) {
        int threatScore = getThreatScore(severity);
        return generateAlert(ipAddress, attackType, severity, threatScore);
    }

    private int getThreatScore(String severity) {
        switch (severity) {
            case "LOW":
                return 20;
            case "MEDIUM":
                return 50;
            case "HIGH":
                return 90;
            default:
                return 20;
        }
    }
}
