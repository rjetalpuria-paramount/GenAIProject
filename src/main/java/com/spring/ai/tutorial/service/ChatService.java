package com.spring.ai.tutorial.service;

import java.util.List;
import java.util.UUID;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

public interface ChatService {

  String getResponse(String prompt, UUID conversationId);

  Flux<String> getStreamingResponse(String prompt, UUID conversationId);

  List<Message> getChatHistory(UUID conversationId);
}
