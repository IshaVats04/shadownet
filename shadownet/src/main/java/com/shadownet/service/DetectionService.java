package com.shadownet.service;

import com.shadownet.Repository.TrafficLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DetectionService {

    @Autowired
    private TrafficLogRepository trafficLogRepository;

    @Autowired
    private AlertService alertService;

    private final ConcurrentHashMap<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private final int DOS_THRESHOLD = 100;
    private final long TIME_WINDOW_MS = 60000; // 1 minute

    public void checkForDosAttack(String ipAddress) {
        long currentTime = System.currentTimeMillis();
        requestCounts.computeIfAbsent(ipAddress, k -> new ArrayList<>()).add(currentTime);
        
        cleanupOldRequests(ipAddress, currentTime);
        
        List<Long> requests = requestCounts.get(ipAddress);
        if (requests.size() >= DOS_THRESHOLD) {
            alertService.generateAlert(ipAddress, "DOS", "HIGH", 90);
            requestCounts.remove(ipAddress); // Reset after alert
        }
    }

    private void cleanupOldRequests(String ipAddress, long currentTime) {
        List<Long> requests = requestCounts.get(ipAddress);
        if (requests != null) {
            requests.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW_MS);
        }
    }
}
