package com.supermarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SupermarketBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupermarketBillingApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("   SUPERMARKET BILLING MANAGEMENT SYSTEM STARTED SUCCESSFULLY!   ");
        System.out.println("   Access Web Application at: http://localhost:8085               ");
        System.out.println("   Access H2 Database Console at: http://localhost:8085/h2-console");
        System.out.println("==================================================================");
    }
}
