package com.audition.configuration;

import com.audition.interceptor.LoggingInterceptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for web services.
 */
@Configuration
public class WebServiceConfiguration implements WebMvcConfigurer {

  private static final String YEAR_MONTH_DAY_PATTERN = "yyyy-MM-dd";

  /**
   * List of interceptors.
   */
  transient List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

  /**
   * Create a bean for MappingJackson2HttpMessageConverter.
   */
  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
      ObjectMapper objectMapper) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    return converter;
  }

  /**
   * Create a bean for ObjectMapper.
   */
  @Bean
  public ObjectMapper objectMapper() {
    // DONE configure Jackson Object mapper that
    //  1. allows for date format as yyyy-MM-dd
    //  2. Does not fail on unknown properties
    //  3. maps to camelCase
    //  4. Does not include null values or empty values
    //  5. does not write datas as timestamps.
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDateFormat(
        new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN, Locale.ENGLISH)); // 1. Set date format
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false); // 2. Ignore unknown properties
    objectMapper.setPropertyNamingStrategy(
        PropertyNamingStrategies.LOWER_CAMEL_CASE); // 3. Use camelCase
    objectMapper.setSerializationInclusion(
        JsonInclude.Include.NON_EMPTY); // 4. Exclude null & empty values
    objectMapper.disable(
        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 5. Prevent timestamp writing
    return objectMapper;
  }

  /**
   * Create a bean for RestTemplate.
   */
  @Bean
  public RestTemplate restTemplate(
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
    final RestTemplate restTemplate = new RestTemplate(
        new BufferingClientHttpRequestFactory(createClientFactory()));
    // DONE use object mapper
    // DONE create a logging interceptor that logs request/response for rest template calls.

    restTemplate.getMessageConverters().add(0, mappingJackson2HttpMessageConverter);
    interceptors.add(new LoggingInterceptor());
    restTemplate.setInterceptors(interceptors);
    return restTemplate;
  }

  private SimpleClientHttpRequestFactory createClientFactory() {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setOutputStreaming(false);
    return requestFactory;
  }
}
