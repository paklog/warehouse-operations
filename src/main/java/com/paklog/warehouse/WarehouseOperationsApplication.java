package com.paklog.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = {"com.paklog.warehouse.domain", "com.paklog.warehouse.adapter.persistence.mongodb", "com.paklog.warehouse.infrastructure.messaging"})
public class WarehouseOperationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseOperationsApplication.class, args);
    }
}
