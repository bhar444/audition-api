package com.audition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * The main entry point for the Audition application.
 * This class bootstraps the Spring Boot application.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.audition")
public class AuditionApplication {

  /**
   * The main method that starts the Spring Boot application.
   *
   * @param args command-line arguments (optional)
   */
  public static void main(final String[] args) {
    SpringApplication.run(AuditionApplication.class, args);
  }

}
