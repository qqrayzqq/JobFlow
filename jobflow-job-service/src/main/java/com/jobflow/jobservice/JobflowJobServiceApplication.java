package com.jobflow.jobservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobflowJobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobflowJobServiceApplication.class, args);
    }

}
