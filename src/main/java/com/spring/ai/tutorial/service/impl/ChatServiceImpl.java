package com.spring.ai.tutorial.service.impl;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

import com.spring.ai.tutorial.service.ChatService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
  private final ChatModel chatModel;
  private ChatMemory chatMemory;
  private ChatClient chatClient;

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
    chatMemory = new InMemoryChatMemory();
    chatClient =
        ChatClient.builder(chatModel)
            .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
            .build();
  }

  public String getResponse(String msg, UUID conversationId) {
    OpenAiChatOptions openAiChatOptions =
        OpenAiChatOptions.builder().model(modelName).topP(topP).temperature(temperature).build();
    Prompt prompt = new Prompt(msg, openAiChatOptions);

    ChatResponse response =
        chatClient
            .prompt(prompt)
            .advisors(
                advisor ->
                    advisor
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemorySize))
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
            .advisors(
                advisor ->
                    advisor
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemorySize))
            .stream()
            .chatResponse();

    return response.mapNotNull(
        resp -> isResponseValid.test(resp) ? resp.getResult().getOutput().getText() : null);
  }

  public List<Message> getChatHistory(UUID conversationId) {
    return chatMemory.get(conversationId.toString(), 100);
  }
}
