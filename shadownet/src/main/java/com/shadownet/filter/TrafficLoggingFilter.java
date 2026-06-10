package com.shadownet.filter;

import com.shadownet.Repository.TrafficLogRepository;
import com.shadownet.model.TrafficLog;
import com.shadownet.service.DetectionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrafficLoggingFilter extends OncePerRequestFilter {

    @Autowired
    private TrafficLogRepository trafficLogRepository;

    @Autowired
    private DetectionService detectionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String ipAddress = getClientIpAddress(request);
        String requestType = request.getMethod();
        String destinationIp = request.getServerName();
        
        // Log the request
        TrafficLog trafficLog = new TrafficLog();
        trafficLog.setSourceIp(ipAddress);
        trafficLog.setDestinationIp(destinationIp);
        trafficLog.setRequestType(requestType);
        trafficLog.setStatus(String.valueOf(response.getStatus()));
        trafficLog.setTimestamp(java.time.LocalDateTime.now());
        
        trafficLogRepository.save(trafficLog);
        
        // Check for DoS attack
        detectionService.checkForDosAttack(ipAddress);
        
        filterChain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
