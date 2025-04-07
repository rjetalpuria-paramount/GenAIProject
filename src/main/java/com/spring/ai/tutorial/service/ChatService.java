package com.spring.ai.tutorial.service;

import reactor.core.publisher.Flux;

public interface ChatService {

  String getResponse(String prompt);

  Flux<String> getStreamingResponse(String prompt);
}
