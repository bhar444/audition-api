package com.audition.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * Interceptor to log request and response details.
 */
@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

  private static final String OUTBOUND_REQUEST_LOG_PREFIX = "\n=== OUTBOUND REQUEST ===\n";
  private static final String INBOUND_RESPONSE_LOG_PREFIX = "\n=== INBOUND RESPONSE ===\n";
  private static final String METHOD_FORMAT = "Method: {}";
  private static final String URI_FORMAT = "URI: {}";
  private static final String HEADERS_FORMAT = "Headers: {}";
  private static final String BODY_FORMAT = "Body: {}";
  private static final String STATUS_FORMAT = "Status: {} {}";


  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution)
      throws IOException {
    logRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    logResponse(response);
    return response;
  }


  private void logRequest(HttpRequest request, byte[] body) {
    if (log.isDebugEnabled()) {
      log.debug(OUTBOUND_REQUEST_LOG_PREFIX);
      log.debug(METHOD_FORMAT, request.getMethod());
      log.debug(URI_FORMAT, request.getURI());
      log.debug(HEADERS_FORMAT, getHeaders(request));
    }
    if (body.length > 0 && log.isDebugEnabled()) {
      log.debug(BODY_FORMAT, new String(body, StandardCharsets.UTF_8));
    }
  }

  private void logResponse(ClientHttpResponse response) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug(INBOUND_RESPONSE_LOG_PREFIX);
      log.debug(STATUS_FORMAT, response.getStatusCode(), response.getStatusText());
      log.debug(HEADERS_FORMAT, getHeaders(response));
    }
    String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
    if (!body.isEmpty()) {
      log.info(BODY_FORMAT, body);
    }
  }

  private String getHeaders(HttpRequest request) {
    return formatHeaders(request.getHeaders());
  }

  private String getHeaders(ClientHttpResponse response) {
    return formatHeaders(response.getHeaders());
  }

  private String formatHeaders(org.springframework.util.MultiValueMap<String, String> headers) {
    if (headers == null || headers.isEmpty()) {
      return "<No Headers>";
    }

    StringBuilder formattedHeaders = new StringBuilder();
    headers.forEach((name, values) ->
        formattedHeaders.append(name).append(": ").append(String.join(",", values)).append(", "));
    return formattedHeaders.length() > 2 ? formattedHeaders.substring(0,
        formattedHeaders.length() - 2) : "";
  }

}
