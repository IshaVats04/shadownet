package com.shadownet.Repository;

import com.shadownet.model.TrafficLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrafficLogRepository extends JpaRepository<TrafficLog, Long> {
    List<TrafficLog> findBySourceIp(String sourceIp);
    List<TrafficLog> findByOrderByTimestampDesc();
}
