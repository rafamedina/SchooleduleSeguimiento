package com.tfg.schooledule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SchooleduleApplication {

  public static void main(String[] args) {
    SpringApplication.run(SchooleduleApplication.class, args);
  }
}
