package com.spring.ai.tutorial.embed.confluence.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ConfluenceClientConfig {

  @Value("${ATL_TOKEN}")
  private String atlassianToken;

  @Bean
  public RequestInterceptor bearerRequestInterceptor() {
    return requestTemplate -> requestTemplate.header("Authorization", "Basic " + atlassianToken);
  }
}
