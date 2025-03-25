package com.audition.common.logging;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Utility class for logging events.
 */
@Component
public class AuditionLogger {

  /**
   * Logs an info message.
   *
   */
  public void info(final Logger logger, final String message) {
    if (logger.isInfoEnabled()) {
      logger.info(message);
    }
  }

  /**
   * Logs an info message with an object.
   */
  public void info(final Logger logger, final String message, final Object object) {
    if (logger.isInfoEnabled()) {
      logger.info(message, object);
    }
  }

  /**
   * Logs a debug message.
   */
  public void debug(final Logger logger, final String message) {
    if (logger.isDebugEnabled()) {
      logger.debug(message);
    }
  }

  /**
   * Logs a warning message.
   */
  public void warn(final Logger logger, final String message) {
    if (logger.isWarnEnabled()) {
      logger.warn(message);
    }
  }

  /**
   * Logs an error message with an exception.
   */
  public void error(final Logger logger, final String message) {
    if (logger.isErrorEnabled()) {
      logger.error(message);
    }
  }

  /**
   * Logs an error message with an exception.
   */
  public void logErrorWithException(final Logger logger, final String message, final Exception e) {
    if (logger.isErrorEnabled()) {
      logger.error(message, e);
    }
  }

  /**
   * Logs an error message with an exception.
   */
  public void logStandardProblemDetail(final Logger logger, final ProblemDetail problemDetail,
      final Exception e) {
    if (logger.isErrorEnabled()) {
      final var message = createStandardProblemDetailMessage(problemDetail);
      logger.error(message, e);
    }
  }

  /**
   * Logs an HTTP status code error.
   */
  public void logHttpStatusCodeError(final Logger logger, final String message,
      final Integer errorCode) {
    if (logger.isErrorEnabled()) {
      logger.error(createBasicErrorResponseMessage(errorCode, message) + "\n");
    }
  }

  /**
   * Creates a standard problem detail message.
   */
  private String createStandardProblemDetailMessage(final ProblemDetail standardProblemDetail) {
    // DONE Add implementation here.
    if (standardProblemDetail == null) {
      return "No problem detail available.";
    }

    StringBuilder sb = new StringBuilder(100);
    sb.append("ProblemDetail: ");

    if (StringUtils.isNotBlank(standardProblemDetail.getTitle())) {
      sb.append("Title=").append(standardProblemDetail.getTitle()).append(", ");
    }
    if (StringUtils.isNotBlank(standardProblemDetail.getDetail())) {
      sb.append("Detail=").append(standardProblemDetail.getDetail()).append(", ");
    }
    sb.append("Status=").append(standardProblemDetail.getStatus()).append(", ");
    if (standardProblemDetail.getInstance() != null) {
      sb.append("Instance=").append(standardProblemDetail.getInstance());
    }

    return sb.toString().trim();
  }

  private String createBasicErrorResponseMessage(final Integer errorCode, final String message) {
    // DONE Add implementation here.
    StringBuilder sb = new StringBuilder(100);
    sb.append("Error Occurred: ");

    if (errorCode != null) {
      sb.append("HTTP Status Code=").append(errorCode).append(", ");
    }
    if (StringUtils.isNotBlank(message)) {
      sb.append("Message=").append(message);
    }

    return sb.toString().trim();
  }

  public boolean isErrorEnabled() {
    return true;
  }
}
