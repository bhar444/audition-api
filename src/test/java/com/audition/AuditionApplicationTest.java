package com.audition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditionApplicationTest {

  @Test
  void contextLoads() {
    Assertions.assertDoesNotThrow(() -> AuditionApplication.main(new String[] {}),
        "Application context should load without exceptions");
  }

}
