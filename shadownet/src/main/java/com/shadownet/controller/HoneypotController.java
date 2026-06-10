package com.shadownet.controller;

import com.shadownet.service.AlertService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/honeypot")
public class HoneypotController {

    @Autowired
    private AlertService alertService;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @GetMapping("/admin-secret")
    public String adminSecret(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        alertService.generateAlert(ipAddress, "HONEYPOT_ACCESS", "HIGH");
        return "ACCESS DENIED - This activity has been logged and reported.";
    }

    @GetMapping("/internal-panel")
    public String internalPanel(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        alertService.generateAlert(ipAddress, "HONEYPOT_ACCESS", "HIGH");
        return "ACCESS DENIED - This activity has been logged and reported.";
    }

    @GetMapping("/config-backup")
    public String configBackup(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        alertService.generateAlert(ipAddress, "HONEYPOT_ACCESS", "MEDIUM");
        return "ACCESS DENIED - This activity has been logged and reported.";
    }

    @GetMapping("/database-dump")
    public String databaseDump(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        alertService.generateAlert(ipAddress, "HONEYPOT_ACCESS", "HIGH");
        return "ACCESS DENIED - This activity has been logged and reported.";
    }
}
