package com.shadownet.service;

import com.shadownet.Repository.AlertRepository;
import com.shadownet.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired(required = false)
    private S3Service s3Service;

    @Autowired(required = false)
    private CloudWatchService cloudWatchService;

    @Autowired(required = false)
    private SNSNotificationService snsNotificationService;

    @Transactional
    public Alert generateAlert(String ipAddress, String attackType, String severity, int threatScore) {
        Alert alert = new Alert();
        alert.setIpAddress(ipAddress);
        alert.setAttackType(attackType);
        alert.setSeverity(severity);
        alert.setThreatScore(threatScore);
        alert.setTimestamp(java.time.LocalDateTime.now());
        
        Alert savedAlert = alertRepository.save(alert);

        // AWS integrations
        if (s3Service != null) {
            try {
                String alertJson = String.format(
                    "{\"id\":%d,\"ipAddress\":\"%s\",\"attackType\":\"%s\",\"severity\":\"%s\",\"threatScore\":%d,\"timestamp\":\"%s\"}",
                    savedAlert.getId(), savedAlert.getIpAddress(), savedAlert.getAttackType(),
                    savedAlert.getSeverity(), savedAlert.getThreatScore(), savedAlert.getTimestamp()
                );
                s3Service.uploadAlertLog(alertJson);
            } catch (Exception e) {
                System.err.println("Error uploading alert to S3: " + e.getMessage());
            }
        }

        if (cloudWatchService != null) {
            cloudWatchService.logAlertCount(1);
            cloudWatchService.logThreatScore(threatScore);
        }

        if (snsNotificationService != null && severity.equals("HIGH")) {
            snsNotificationService.sendHighPriorityAlert(ipAddress, attackType);
        }

        return savedAlert;
    }

    @Transactional
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
