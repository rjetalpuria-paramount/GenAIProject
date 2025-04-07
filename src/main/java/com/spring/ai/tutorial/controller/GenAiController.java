package com.spring.ai.tutorial.controller;

import com.spring.ai.tutorial.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/genAI")
@RequiredArgsConstructor
public class GenAiController {
  private final ChatService chatService;

  @GetMapping("/chat")
  public ResponseEntity<String> getChatResponse(@RequestParam String prompt) {
    String response = chatService.getResponse(prompt);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/streaming-chat")
  public ResponseEntity<Flux<String>> getStreamingResponse(@RequestParam String prompt) {
    Flux<String> response = chatService.getStreamingResponse(prompt);
    return ResponseEntity.ok(response);
  }
}
