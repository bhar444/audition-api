package com.audition.configuration;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Filter to inject OpenTelemetry trace and span Ids in the response headers.
 */
@Component
public class ResponseHeaderInjector implements Filter {

  // DONE Inject openTelemetry trace and span Ids in the response headers.
  private static final String TRACE_ID_HEADER = "X-Trace-Id";
  private static final String SPAN_ID_HEADER = "X-Span-Id";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    if (response instanceof HttpServletResponse httpResponse) {
      Span currentSpan = Span.current();
      if (currentSpan != null) {
        SpanContext spanContext = currentSpan.getSpanContext();
        if (spanContext != null && spanContext.isValid()) {
          httpResponse.setHeader(TRACE_ID_HEADER, spanContext.getTraceId());
          httpResponse.setHeader(SPAN_ID_HEADER, spanContext.getSpanId());
        }
      }
    }

    chain.doFilter(request, response);
  }

}
