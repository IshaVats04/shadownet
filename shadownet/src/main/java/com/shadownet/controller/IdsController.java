package com.shadownet.controller;

import com.shadownet.Repository.AlertRepository;
import com.shadownet.Repository.TrafficLogRepository;
import com.shadownet.model.Alert;
import com.shadownet.model.TrafficLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analyst")
public class IdsController {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private TrafficLogRepository trafficLogRepository;

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public List<Alert> getAllAlerts() {
        return alertRepository.findByOrderByTimestampDesc();
    }

    @GetMapping("/traffic-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public List<TrafficLog> getAllTrafficLogs() {
        return trafficLogRepository.findAll();
    }

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAlerts", alertRepository.count());
        stats.put("totalTrafficLogs", trafficLogRepository.count());
        stats.put("recentAlerts", alertRepository.findByOrderByTimestampDesc().subList(0, Math.min(10, (int) alertRepository.count())));
        return stats;
    }
}
