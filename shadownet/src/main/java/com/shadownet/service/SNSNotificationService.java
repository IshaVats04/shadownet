package com.shadownet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

@Service
@Profile("aws")
public class SNSNotificationService {

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.sns.topic-arn}")
    private String topicArn;

    public void sendAlertNotification(String ipAddress, String attackType, String severity, String message) {
        try {
            String subject = String.format("ShadowNet Alert: %s Attack Detected - %s", severity, attackType);
            String fullMessage = String.format(
                "Security Alert Generated\n\n" +
                "IP Address: %s\n" +
                "Attack Type: %s\n" +
                "Severity: %s\n" +
                "Timestamp: %s\n\n" +
                "Details: %s",
                ipAddress, attackType, severity, 
                java.time.LocalDateTime.now().toString(),
                message
            );

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(fullMessage)
                    .build();

            snsClient.publish(request);
            
        } catch (SnsException e) {
            System.err.println("Error sending SNS notification: " + e.getMessage());
        }
    }

    public void sendHighPriorityAlert(String ipAddress, String attackType) {
        sendAlertNotification(ipAddress, attackType, "HIGH", 
            "Immediate attention required. High severity attack detected.");
    }

    public void sendDosAlert(String ipAddress, int requestCount) {
        String message = String.format(
            "DoS attack detected from IP %s. Request count: %d within threshold window.",
            ipAddress, requestCount
        );
        sendAlertNotification(ipAddress, "DOS", "HIGH", message);
    }

    public void sendBruteForceAlert(String ipAddress, int failedAttempts) {
        String message = String.format(
            "Brute force attack detected from IP %s. Failed login attempts: %d",
            ipAddress, failedAttempts
        );
        sendAlertNotification(ipAddress, "BRUTE_FORCE", "MEDIUM", message);
    }

    public void subscribeToTopic(String email) {
        try {
            SubscribeRequest request = SubscribeRequest.builder()
                    .topicArn(topicArn)
                    .protocol("email")
                    .endpoint(email)
                    .returnSubscriptionArn(true)
                    .build();

            SubscribeResponse response = snsClient.subscribe(request);
            System.out.println("Subscription ARN: " + response.subscriptionArn());
            
        } catch (SnsException e) {
            System.err.println("Error subscribing to SNS topic: " + e.getMessage());
        }
    }
}
