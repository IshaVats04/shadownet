package com.shadownet.Repository;

import com.shadownet.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findBySeverity(String severity);
    List<Alert> findByIpAddress(String ipAddress);
    List<Alert> findByOrderByTimestampDesc();
}