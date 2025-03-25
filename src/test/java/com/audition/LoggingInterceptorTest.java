package com.audition;

class LoggingInterceptorTest {
//
//    private LoggingInterceptor interceptor;
//
//    @Mock
//    private HttpRequest request;
//
//    @Mock
//    private ClientHttpResponse response;
//
//    @Mock
//    private ClientHttpRequestExecution execution;
//
//    @Mock
//    private Logger logger;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//
//    @Test
//    void testIntercept_ShouldLogRequest() throws IOException {
//        byte[] requestBody = "Test Request Body".getBytes(StandardCharsets.UTF_8);
//
//        when(request.getMethod()).thenReturn(HttpMethod.POST);
//        when(request.getURI()).thenReturn(URI.create("https://api.example.com"));
//        when(execution.execute(request, requestBody)).thenReturn(response);
//        when(response.getBody()).thenReturn(
//            new ByteArrayInputStream("Test Response Body".getBytes(StandardCharsets.UTF_8)));
//        when(response.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
//
//        interceptor.intercept(request, requestBody, execution);
//
//        verify(execution).execute(request, requestBody);
//    }
//
//
//    @Test
//    void testIntercept_ShouldHandleEmptyRequestBody() throws IOException {
//        byte[] requestBody = new byte[0]; // Empty request body
//
//        when(request.getMethod()).thenReturn(HttpMethod.GET);
//        when(request.getURI()).thenReturn(URI.create("https://api.example.com"));
//        when(execution.execute(request, requestBody)).thenReturn(response);
//        when(response.getBody()).thenReturn(new ByteArrayInputStream("Response Data".getBytes(StandardCharsets.UTF_8)));
//        when(response.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
//        when(request.getHeaders()).thenReturn(new HttpHeaders());
//        interceptor.intercept(request, requestBody, execution);
//
//        verify(execution).execute(request, requestBody);
//    }
//
//
//    @Test
//    void testIntercept_ShouldLogResponse() throws IOException {
//        byte[] requestBody = "Test Request".getBytes(StandardCharsets.UTF_8);
//        String responseBody = "Test Response";
//        interceptor.interceptResponse();
//        when(request.getMethod()).thenReturn(HttpMethod.GET);
//        when(request.getURI()).thenReturn(URI.create("https://api.example.com"));
//        when(execution.execute(request, requestBody)).thenReturn(response);
//        when(response.getBody()).thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
//        when(response.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
//
//
//
//        ClientHttpResponse interceptedResponse = interceptor.intercept(request, requestBody, execution);
//
//        assertEquals(response, interceptedResponse, "Intercepted response should match the executed response");
//        verify(execution).execute(request, requestBody);
//        verify(logger).debug("\n=== INBOUND RESPONSE ===\n");
//        verify(logger).debug("Status: {} {}", org.springframework.http.HttpStatus.OK, "OK");
//        verify(logger).debug("Headers: {}", ""); // Adjust as necessary for your headers
//        verify(logger).info("Body: {}", responseBody);
//    }
//
//
//    @Test
//    void testIntercept_ShouldHandleEmptyResponseBody() throws IOException {
//        byte[] requestBody = "Test Request".getBytes(StandardCharsets.UTF_8);
//        String responseBody = ""; // Empty response
//
//        when(request.getMethod()).thenReturn(HttpMethod.GET);
//        when(request.getURI()).thenReturn(URI.create("https://api.example.com"));
//        when(execution.execute(request, requestBody)).thenReturn(response);
//        when(response.getBody()).thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
//        when(response.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.NO_CONTENT);
//
//        interceptor.intercept(request, requestBody, execution);
//
//        verify(execution).execute(request, requestBody);
//    }
}