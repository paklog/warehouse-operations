package com.paklog.warehouse.domain.licenseplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DefaultLicensePlateGenerator implements LicensePlateGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DefaultLicensePlateGenerator.class);
    
    private static final String PREFIX = "LP";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public LicensePlateId generateLicensePlate() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        long sequence = counter.getAndIncrement();
        String licensePlateValue = String.format("%s%s%03d", PREFIX, timestamp, sequence % 1000);
        
        logger.debug("Generated license plate: {}", licensePlateValue);
        return LicensePlateId.of(licensePlateValue);
    }

    @Override
    public LicensePlateId generateLicensePlate(String prefix) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        long sequence = counter.getAndIncrement();
        String licensePlateValue = String.format("%s%s%03d", 
                prefix != null ? prefix : PREFIX, timestamp, sequence % 1000);
        
        logger.debug("Generated license plate with prefix {}: {}", prefix, licensePlateValue);
        return LicensePlateId.of(licensePlateValue);
    }
}