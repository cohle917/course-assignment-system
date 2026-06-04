package com.teaching.homework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.teaching.common.entity"})
@EnableJpaRepositories(basePackages = {"com.teaching.homework.repository"})
public class HomeworkServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeworkServiceApplication.class, args);
    }
}
