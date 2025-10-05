package com.spring.ai.tutorial.chat.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
  private final ChatModel chatModel;
  private final JdbcTemplate jdbcTemplate;
  private final PgVectorStore vectorStore;
  private ChatClient chatClient;
  private ChatMemory chatMemory;

  @Value("${com.ai.springAiTutorial.model}")
  private String modelName;

  @Value("${com.ai.springAiTutorial.topP}")
  private Double topP;

  @Value("${com.ai.springAiTutorial.temperature}")
  private Double temperature;

  @Value("${com.ai.springAiTutorial.chatMemorySize}")
  private Integer chatMemorySize;

  private final Predicate<ChatResponse> isResponseValid =
      response ->
          response != null
              && response.getResult() != null
              && response.getResult().getOutput() != null;

  @PostConstruct
  void init() {
    ChatMemoryRepository chatMemoryRepository =
        JdbcChatMemoryRepository.builder().jdbcTemplate(jdbcTemplate).build();
    chatMemory =
        MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(chatMemorySize)
            .build();
    chatClient =
        ChatClient.builder(chatModel)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
  }

  public String getResponse(String msg, UUID conversationId) {
    OpenAiChatOptions openAiChatOptions =
        OpenAiChatOptions.builder().model(modelName).topP(topP).temperature(temperature).build();
    Prompt prompt = new Prompt(msg, openAiChatOptions);

    ChatResponse response =
        chatClient
            .prompt(prompt)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .chatResponse();
    return isResponseValid.test(response) ? response.getResult().getOutput().getText() : null;
  }

  public Flux<String> getStreamingResponse(String msg, UUID conversationId) {
    OpenAiChatOptions openAiChatOptions =
        OpenAiChatOptions.builder()
            .model(modelName)
            .topP(topP)
            .temperature(temperature)
            .streamUsage(true)
            .build();

    Flux<ChatResponse> response =
        chatClient
            .prompt(new Prompt(msg, openAiChatOptions))
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .stream()
            .chatResponse();

    return response.mapNotNull(
        resp -> isResponseValid.test(resp) ? resp.getResult().getOutput().getText() : null);
  }

  public List<Message> getChatHistory(UUID conversationId) {
    return chatMemory.get(conversationId.toString());
  }

  public void saveEmbedding(String message) {
    vectorStore.add(List.of(new Document(message)));
  }
}
