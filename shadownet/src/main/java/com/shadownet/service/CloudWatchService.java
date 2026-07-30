package com.shadownet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;

@Service
@Profile("aws")
public class CloudWatchService {

    @Autowired
    private CloudWatchClient cloudWatchClient;

    private static final String NAMESPACE = "ShadowNet/IDS";

    public void logMetric(String metricName, double value, String unit) {
        try {
            MetricDatum datum = MetricDatum.builder()
                    .metricName(metricName)
                    .unit(StandardUnit.fromValue(unit))
                    .value(value)
                    .timestamp(Instant.now())
                    .build();

            PutMetricDataRequest request = PutMetricDataRequest.builder()
                    .namespace(NAMESPACE)
                    .metricData(datum)
                    .build();

            cloudWatchClient.putMetricData(request);
            
        } catch (CloudWatchException e) {
            System.err.println("Error logging to CloudWatch: " + e.getMessage());
        }
    }

    public void logAlertCount(int count) {
        logMetric("AlertCount", count, "Count");
    }

    public void logTrafficRequestCount(int count) {
        logMetric("TrafficRequestCount", count, "Count");
    }

    public void logThreatScore(double score) {
        logMetric("AverageThreatScore", score, "None");
    }

    public void logFailedLoginAttempts(int count) {
        logMetric("FailedLoginAttempts", count, "Count");
    }

    public void logDosAttackDetected(boolean detected) {
        logMetric("DosAttackDetected", detected ? 1 : 0, "Count");
    }

    public void createCustomMetric(String metricName) {
        try {
            // CloudWatch automatically creates metrics when data is published
            // This method is for documentation purposes
            System.out.println("Custom metric will be created when first data point is sent: " + metricName);
            
        } catch (Exception e) {
            System.err.println("Error with custom metric: " + e.getMessage());
        }
    }
}
