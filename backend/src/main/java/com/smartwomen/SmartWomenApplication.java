package com.smartwomen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SmartWomenApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartWomenApplication.class, args);
        System.out.println("🚀 SmartWomen CRM - AI-Powered Customer Orchestrator");
        System.out.println("🌎 Ready to empower women-led businesses in LATAM!");
    }
}