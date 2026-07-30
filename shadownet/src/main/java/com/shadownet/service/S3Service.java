package com.shadownet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Profile("aws")
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public void uploadAlertLog(String alertData) {
        String fileName = "alerts/alert-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json";
        
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putRequest, 
                RequestBody.fromString(alertData));
            
        } catch (S3Exception e) {
            System.err.println("Error uploading to S3: " + e.getMessage());
        }
    }

    public void uploadTrafficLog(String trafficData) {
        String fileName = "traffic-logs/traffic-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json";
        
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putRequest, 
                RequestBody.fromString(trafficData));
            
        } catch (S3Exception e) {
            System.err.println("Error uploading to S3: " + e.getMessage());
        }
    }

    public String downloadFile(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsString(getRequest);
            
        } catch (S3Exception e) {
            System.err.println("Error downloading from S3: " + e.getMessage());
            return null;
        }
    }

    public void createBucketIfNotExists() {
        try {
            HeadBucketRequest headRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.headBucket(headRequest);
            
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                try {
                    CreateBucketRequest createRequest = CreateBucketRequest.builder()
                            .bucket(bucketName)
                            .build();
                    
                    s3Client.createBucket(createRequest);
                    System.out.println("Created S3 bucket: " + bucketName);
                    
                } catch (S3Exception createException) {
                    System.err.println("Error creating bucket: " + createException.getMessage());
                }
            }
        }
    }
}
