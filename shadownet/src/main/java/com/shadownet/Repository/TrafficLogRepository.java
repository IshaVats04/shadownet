package com.shadownet.Repository;

import com.shadownet.model.TrafficLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrafficLogRepository extends JpaRepository<TrafficLog, Long> {
    List<TrafficLog> findBySourceIp(String sourceIp);
    List<TrafficLog> findByOrderByTimestampDesc();

    // Pagination support
    Page<TrafficLog> findBySourceIp(String sourceIp, Pageable pageable);
    Page<TrafficLog> findAll(Pageable pageable);

    // Custom JPQL with date range filtering
    @Query("SELECT t FROM TrafficLog t WHERE t.timestamp BETWEEN :startDate AND :endDate ORDER BY t.timestamp DESC")
    List<TrafficLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Aggregate functions - Count requests by IP
    @Query("SELECT t.sourceIp, COUNT(t) FROM TrafficLog t WHERE t.timestamp >= :since GROUP BY t.sourceIp")
    List<Object[]> countRequestsByIp(@Param("since") LocalDateTime since);

    // Aggregate functions - Count by request type
    @Query("SELECT t.requestType, COUNT(t) FROM TrafficLog t GROUP BY t.requestType")
    List<Object[]> countByRequestType();

    // Complex query with HAVING clause for detecting suspicious IPs
    @Query("SELECT t.sourceIp, COUNT(t) FROM TrafficLog t WHERE t.timestamp >= :since GROUP BY t.sourceIp HAVING COUNT(t) > :threshold")
    List<Object[]> findHighTrafficIPs(@Param("since") LocalDateTime since, @Param("threshold") long threshold);

    // Native SQL for time-based analytics
    @Query(value = "SELECT DATE_FORMAT(timestamp, '%Y-%m-%d %H:00:00') as hour, request_type, COUNT(*) as count FROM traffic_logs WHERE timestamp >= :startDate GROUP BY DATE_FORMAT(timestamp, '%Y-%m-%d %H:00:00'), request_type ORDER BY hour DESC", nativeQuery = true)
    List<Object[]> getHourlyTrafficStats(@Param("startDate") LocalDateTime startDate);

    // JOIN-like operation to get traffic logs with alert information
    @Query("SELECT t FROM TrafficLog t WHERE t.sourceIp IN (SELECT a.ipAddress FROM Alert a WHERE a.timestamp >= :since)")
    List<TrafficLog> findTrafficFromAlertIPs(@Param("since") LocalDateTime since);
}
