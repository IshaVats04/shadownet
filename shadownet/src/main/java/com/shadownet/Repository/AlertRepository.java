package com.shadownet.Repository;

import com.shadownet.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findBySeverity(String severity);
    List<Alert> findByIpAddress(String ipAddress);
    List<Alert> findByOrderByTimestampDesc();

    // Pagination support
    Page<Alert> findBySeverity(String severity, Pageable pageable);
    Page<Alert> findByIpAddress(String ipAddress, Pageable pageable);
    Page<Alert> findAll(Pageable pageable);

    // Custom JPQL with date range filtering
    @Query("SELECT a FROM Alert a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<Alert> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Aggregate functions - Count by severity
    @Query("SELECT a.severity, COUNT(a) FROM Alert a GROUP BY a.severity")
    List<Object[]> countAlertsBySeverity();

    // Aggregate functions - Count by attack type
    @Query("SELECT a.attackType, COUNT(a) FROM Alert a GROUP BY a.attackType")
    List<Object[]> countAlertsByAttackType();

    // Aggregate functions - Average threat score by severity
    @Query("SELECT a.severity, AVG(a.threatScore) FROM Alert a GROUP BY a.severity")
    List<Object[]> getAverageThreatScoreBySeverity();

    // Complex query with filtering and aggregation
    @Query("SELECT a.ipAddress, COUNT(a), MAX(a.threatScore) FROM Alert a WHERE a.timestamp >= :since GROUP BY a.ipAddress HAVING COUNT(a) > :threshold")
    List<Object[]> findHighRiskIPs(@Param("since") LocalDateTime since, @Param("threshold") long threshold);

    // Native SQL query for complex analytics
    @Query(value = "SELECT DATE_FORMAT(timestamp, '%Y-%m-%d') as date, severity, COUNT(*) as count FROM alerts WHERE timestamp >= :startDate GROUP BY DATE_FORMAT(timestamp, '%Y-%m-%d'), severity ORDER BY date DESC", nativeQuery = true)
    List<Object[]> getDailyAlertStats(@Param("startDate") LocalDateTime startDate);
}