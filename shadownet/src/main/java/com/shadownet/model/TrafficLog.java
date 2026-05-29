package com.shadownet.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "traffic_logs")

public class TrafficLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceIp;
    private String destinationIp;
    private String requestType;
    private String status;
    private LocalDateTime timestamp;
    public TrafficLog() {
    }
    public Long getId() {
        return id;
    }
    public String getSourceIp() {
        return sourceIp;
    }
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
    public String getDestinationIp() {
        return destinationIp;
    }
    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }
    public String getRequestType() {
        return requestType;
    }   
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
