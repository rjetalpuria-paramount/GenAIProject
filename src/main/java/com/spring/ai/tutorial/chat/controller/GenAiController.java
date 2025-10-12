package com.spring.ai.tutorial.chat.controller;

import com.spring.ai.tutorial.chat.service.ChatService;
import com.spring.ai.tutorial.confluence.service.ConfluenceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
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
  private final ConfluenceService confluenceService;
  private static final String CHAT_ID_HEADER = "chat-id";

  @GetMapping("/chat")
  public ResponseEntity<String> getChatResponse(
      @RequestParam String prompt, @RequestParam(required = false) UUID chatId) {
    if (chatId == null) {
      chatId = UUID.randomUUID();
    }
    String response = chatService.getResponse(prompt, chatId);
    return ResponseEntity.ok().header(CHAT_ID_HEADER, chatId.toString()).body(response);
  }

  @GetMapping("/streaming-chat")
  public ResponseEntity<Flux<String>> getStreamingResponse(
      @RequestParam String prompt, @RequestParam(required = false) UUID chatId) {
    if (chatId == null) {
      chatId = UUID.randomUUID();
    }
    Flux<String> response = chatService.getStreamingResponse(prompt, chatId);
    return ResponseEntity.ok().header(CHAT_ID_HEADER, chatId.toString()).body(response);
  }

  @GetMapping("/chat-history")
  public ResponseEntity<List<Message>> getChatHistory(@RequestParam UUID chatId) {
    List<Message> chatHistory = chatService.getChatHistory(chatId);
    return ResponseEntity.ok().body(chatHistory);
  }

  @Deprecated
  @GetMapping("/embed")
  public ResponseEntity<Void> saveEmbedding(@RequestParam String message) {
    chatService.saveEmbedding(message);
    return ResponseEntity.ok().build();
  }

  // TODO: Move this to a dedicated embedding controller
  @GetMapping("/embed-confluence-pages")
  public ResponseEntity<Void> embedConfluencePages(
      @RequestParam(required = false) String documentId) {
    if (StringUtils.isNotBlank(documentId)) {
      confluenceService.embedConfluenceDocumentById(documentId);
    } else {
      confluenceService.embedAllConfluenceDocuments();
    }
    return ResponseEntity.ok().build();
  }
}
