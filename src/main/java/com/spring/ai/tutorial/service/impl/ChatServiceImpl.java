package com.spring.ai.tutorial.service.impl;

import com.spring.ai.tutorial.service.ChatService;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
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
  private final StreamingChatModel streamingChatModel;

  @Value("${com.ai.springAiTutorial.model}")
  private String modelName;

  @Value("${com.ai.springAiTutorial.topP}")
  private Double topP;

  @Value("${com.ai.springAiTutorial.temperature}")
  private Double temperature;

  private final Function<ChatResponse, Boolean> isResponseValid =
      response ->
          response != null
              && response.getResult() != null
              && response.getResult().getOutput() != null;

  public String getResponse(String msg) {
    OpenAiChatOptions openAiChatOptions =
        OpenAiChatOptions.builder().model(modelName).topP(topP).temperature(temperature).build();

    Prompt prompt = new Prompt(msg, openAiChatOptions);
    ChatResponse response = chatModel.call(prompt);
    return response.getResult().getOutput().getText();
  }

  public Flux<String> getStreamingResponse(String msg) {
    OpenAiChatOptions openAiChatOptions =
        OpenAiChatOptions.builder()
            .model(modelName)
            .topP(topP)
            .temperature(temperature)
            .streamUsage(true)
            .build();

    Prompt prompt = new Prompt(msg, openAiChatOptions);
    return streamingChatModel.stream(prompt)
        .mapNotNull(
            response -> {
              if (Boolean.TRUE.equals(isResponseValid.apply(response))) {
                return response.getResult().getOutput().getText();
              } else {
                return null;
              }
            });

//    return streamingChatModel.stream(prompt).map(response -> response.getResult().getOutput().getText());
  }
}
