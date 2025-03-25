package com.audition;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.logging.AuditionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;

class AuditionLoggerTest {

  private transient AuditionLogger auditionLogger;

  @SuppressWarnings("PMD.LoggerIsNotStaticFinal")
  @Mock
  private transient Logger logger;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    auditionLogger = new AuditionLogger();
  }

  @Test
  void testLogStandardProblemDetailShouldFormatCorrectly() {
    when(logger.isErrorEnabled()).thenReturn(true);
    ProblemDetail problemDetail = ProblemDetail.forStatus(400);
    problemDetail.setTitle("Invalid Request");
    auditionLogger.logStandardProblemDetail(logger, problemDetail, new Exception("Test Exception"));
    if (logger.isErrorEnabled()) {
      verify(logger).error(contains("Invalid Request"), any(Exception.class));
    }

  }

  @Test
  void testInfoShouldLogMessageWhenInfoEnabled() {
    when(logger.isInfoEnabled()).thenReturn(true);

    auditionLogger.info(logger, "Test Info Message");

    verify(logger).info("Test Info Message");
  }

  @Test
  void testInfoWithObjectShouldLogMessageWhenInfoEnabled() {
    when(logger.isInfoEnabled()).thenReturn(true);

    auditionLogger.info(logger, "Test Info Message with Object", 123);

    verify(logger).info("Test Info Message with Object", 123);
  }

  @Test
  void testDebugShouldLogMessageWhenDebugEnabled() {
    when(logger.isDebugEnabled()).thenReturn(true);

    auditionLogger.debug(logger, "Test Debug Message");

    verify(logger).debug("Test Debug Message");
  }

  @Test
  void testWarnShouldLogMessageWhenWarnEnabled() {
    when(logger.isWarnEnabled()).thenReturn(true);

    auditionLogger.warn(logger, "Test Warn Message");

    verify(logger).warn("Test Warn Message");
  }

  @Test
  void testErrorShouldLogMessageWhenErrorEnabled() {
    when(logger.isErrorEnabled()).thenReturn(true);

    auditionLogger.error(logger, "Test Error Message");

    verify(logger).error("Test Error Message");
  }

  @Test
  void testLogErrorWithExceptionShouldLogExceptionWhenErrorEnabled() {
    when(logger.isErrorEnabled()).thenReturn(true);
    Exception exception = new RuntimeException("Test Exception");

    auditionLogger.logErrorWithException(logger, "Error occurred", exception);

    verify(logger).error("Error occurred", exception);
  }

  @Test
  void testLogStandardProblemDetailShouldLogMessageWhenErrorEnabled() {
    when(logger.isErrorEnabled()).thenReturn(true);
    ProblemDetail problemDetail = ProblemDetail.forStatus(400);
    problemDetail.setTitle("Bad Request");
    Exception exception = new RuntimeException("Test Exception");
    auditionLogger.logStandardProblemDetail(logger, problemDetail, exception);
    if (logger.isErrorEnabled()) {
      verify(logger).error(contains("Bad Request"), eq(exception));
    }
  }

  @Test
  void testLogHttpStatusCodeErrorShouldLogMessageWhenErrorEnabled() {
    when(logger.isErrorEnabled()).thenReturn(true);

    auditionLogger.logHttpStatusCodeError(logger, "Service Unavailable", 503);

    if (logger.isErrorEnabled()) {
      verify(logger).error(contains("HTTP Status Code=503, Message=Service Unavailable"));
    }

  }
}