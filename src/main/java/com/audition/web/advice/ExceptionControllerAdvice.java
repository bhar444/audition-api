package com.audition.web.advice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import io.micrometer.common.util.StringUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/**
 * Controller Advice for handling exceptions.
 */
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

  public static final String DEFAULT_TITLE = "API Error Occurred";
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
  private static final String ERROR_MESSAGE =
      " Error Code from Exception could not be mapped to a valid HttpStatus Code - ";
  private static final String DEFAULT_MESSAGE =
      "API Error occurred. Please contact support or administrator.";
  private static final Map<Class<? extends Exception>, HttpStatusCode> EXCEPTION_TO_STATUS_MAP =
      new ConcurrentHashMap<>();

  static {
    EXCEPTION_TO_STATUS_MAP.put(HttpClientErrorException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_TO_STATUS_MAP.put(HttpRequestMethodNotSupportedException.class, METHOD_NOT_ALLOWED);
    EXCEPTION_TO_STATUS_MAP.put(IllegalArgumentException.class, HttpStatus.BAD_REQUEST);
    EXCEPTION_TO_STATUS_MAP.put(SystemException.class, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Logger instance for logging events.
   */
  private final transient AuditionLogger auditionLogger;

  /**
   * Constructor for ExceptionControllerAdvice.
   *
   * @param auditionLogger the logger for logging events
   */
  public ExceptionControllerAdvice(AuditionLogger auditionLogger) {
    super();
    this.auditionLogger = auditionLogger;
  }

  /**
   * Handles HttpClientErrorException and returns a ProblemDetail.
   *
   */
  @ExceptionHandler(HttpClientErrorException.class)
  public ProblemDetail handleHttpClientException(final HttpClientErrorException e) {
    return createProblemDetail(e, e.getStatusCode());

  }

  /**
   * Handles Exception and returns a ProblemDetail.
   *
   */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleMainException(final Exception e) {
    // DONE Add handling for Exception
    final HttpStatusCode status = getHttpStatusCodeFromException(e);
    if (logger.isErrorEnabled()) {
      auditionLogger.error(LOG, "Exception occurred: " + e);
    }
    return createProblemDetail(e, status);

  }

  /**
   * Handles SystemException and returns a ProblemDetail.
   */
  @ExceptionHandler(SystemException.class)
  public ProblemDetail handleSystemException(final SystemException e) {
    // DONE Add Handling for SystemException
    final HttpStatusCode status = getHttpStatusCodeFromSystemException(e);
    if (logger.isErrorEnabled()) {
      auditionLogger.error(LOG, "System Exception occurred: " + e);
    }
    return createProblemDetail(e, status);

  }


  private ProblemDetail createProblemDetail(final Exception exception,
      final HttpStatusCode statusCode) {
    final ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
    problemDetail.setDetail(getMessageFromException(exception));
    if (exception instanceof SystemException) {
      problemDetail.setTitle(((SystemException) exception).getTitle());
    } else {
      problemDetail.setTitle(DEFAULT_TITLE);
    }
    return problemDetail;
  }

  private String getMessageFromException(final Exception exception) {
    if (StringUtils.isNotBlank(exception.getMessage())) {
      return exception.getMessage();
    }
    return DEFAULT_MESSAGE;
  }

  /**
   * Returns the HttpStatusCode from a SystemException.
   */
  public HttpStatusCode getHttpStatusCodeFromSystemException(final SystemException exception) {
    try {
      return HttpStatusCode.valueOf(exception.getStatusCode());
    } catch (final IllegalArgumentException iae) {
      if (logger.isInfoEnabled()) {
        auditionLogger.info(LOG, ERROR_MESSAGE + exception.getStatusCode());
      }
      return INTERNAL_SERVER_ERROR;
    }
  }

  /**
   * Returns the HttpStatusCode from an Exception.
   */
  public HttpStatusCode getHttpStatusCodeFromException(final Exception exception) {

    HttpStatusCode status = EXCEPTION_TO_STATUS_MAP.get(exception.getClass());

    if (status != null) {
      return status;
    }

    if (logger.isInfoEnabled()) {
      auditionLogger.info(LOG, ERROR_MESSAGE + exception);
    }

    return INTERNAL_SERVER_ERROR;
  }
}



