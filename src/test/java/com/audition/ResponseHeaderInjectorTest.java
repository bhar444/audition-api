package com.audition;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.configuration.ResponseHeaderInjector;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;


class ResponseHeaderInjectorTest {

  public static final String X_TRACE_ID = "X-Trace-Id";
  public static final String TRACE_123 = "trace-123";
  public static final String SPAN_456 = "span-456";
  public static final String SPAN_ID = "X-Span-Id";
  private transient ResponseHeaderInjector filter;

  @Mock
  private transient HttpServletResponse response;

  @Mock
  private transient ServletRequest request;

  @Mock
  private transient FilterChain chain;

  @Mock
  private transient Span span;

  @Mock
  private transient SpanContext spanContext;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    filter = new ResponseHeaderInjector();
  }

  @Test
  void testDoFilterShouldNotThrowExceptions() throws Exception {
    // Arrange
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      // Act & Assert
      assertDoesNotThrow(() -> filter.doFilter(request, response, chain),
          "Filter should execute without throwing exceptions");
    }
  }

  @Test
  void testDoFilterShouldInjectTraceIdHeader() throws Exception {
    // Arrange
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      // Act
      filter.doFilter(request, response, chain);

      // Assert
      verify(response).setHeader(X_TRACE_ID, TRACE_123);
    }
  }

  @Test
  void testDoFilterShouldInjectSpanIdHeader() throws Exception {
    // Arrange
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      // Act
      filter.doFilter(request, response, chain);

      // Assert
      verify(response).setHeader(SPAN_ID, SPAN_456);
    }
  }

  @Test
  void testDoFilterShouldCallFilterChain() throws Exception {
    // Arrange
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      // Act
      filter.doFilter(request, response, chain);

      // Assert
      verify(chain).doFilter(request, response);
    }
  }

  @Test
  void testDoFilterShouldSetTraceIdHeader1() throws Exception {
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      filter.doFilter(request, response, chain);

      verify(response).setHeader(X_TRACE_ID, TRACE_123);
    }
  }

  @Test
  void testDoFilterShouldSetSpanIdHeader1() throws Exception {
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      filter.doFilter(request, response, chain);

      verify(response).setHeader(SPAN_ID, SPAN_456);
    }
  }

  @Test
  void testDoFilterShouldInvokeFilterChain1() throws Exception {
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      filter.doFilter(request, response, chain);

      verify(chain).doFilter(request, response);
    }
  }

  @Test
  void testDoFilterShouldSetTraceIdHeader() throws Exception {
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      filter.doFilter(request, response, chain);

      verify(response).setHeader(X_TRACE_ID, TRACE_123);
    }
  }

  @Test
  void testDoFilterShouldSetSpanIdHeader() throws Exception {
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      filter.doFilter(request, response, chain);

      verify(response).setHeader(SPAN_ID, SPAN_456);
    }
  }

  @Test
  void testDoFilterShouldInvokeFilterChain() throws Exception {
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      filter.doFilter(request, response, chain);

      verify(chain).doFilter(request, response);
    }
  }

  @Test
  void testDoFilterShouldInvokeHeadersInCorrectOrder() throws Exception {
    try (MockedStatic<Span> mockedSpan = mockStatic(Span.class)) {
      setupMocks(mockedSpan);

      filter.doFilter(request, response, chain);


      InOrder inOrder = inOrder(response, chain);
      inOrder.verify(response, times(1)).setHeader(X_TRACE_ID, TRACE_123);
      inOrder.verify(response, times(1)).setHeader(SPAN_ID, SPAN_456);
      inOrder.verify(chain, times(1)).doFilter(request, response);
    }
  }


  // Helper method to reduce duplication
  private void setupMocks(MockedStatic<Span> mockedSpan) {
    when(span.getSpanContext()).thenReturn(spanContext);
    when(spanContext.isValid()).thenReturn(true);
    when(spanContext.getTraceId()).thenReturn(TRACE_123);
    when(spanContext.getSpanId()).thenReturn(SPAN_456);
    mockedSpan.when(Span::current).thenReturn(span);
  }
}

