package com.spring.ai.tutorial.slack.service;

import com.slack.api.Slack;
import com.slack.api.app_backend.slash_commands.SlashCommandResponseSender;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.spring.ai.tutorial.chat.service.ChatService;
import com.spring.ai.tutorial.slack.model.SlackJobPayload;
import java.io.IOException;
import java.util.Queue;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackJobService {
  private final ChatService chatService;
  private final SlashCommandResponseSender slashCommandResponseSender;

  @Value("${SLACK_BOT_TOKEN:}")
  private String slackBotToken;

  Queue<SlackJobPayload> jobQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

  public void addJob(SlackJobPayload job) {
    jobQueue.offer(job);
  }

  public void processNextJob() {
    SlackJobPayload job = jobQueue.poll();
    if (job == null) {
      return;
    }
    UUID chatId = UUID.randomUUID();
    String response = chatService.getResponse(job.text(), chatId);
    if (response == null) {
      response = "I couldn't generate a response.";
    }

    if (job.responseUrl() != null) {
      sendSlashCommandResponse(job.responseUrl(), response);
    } else if (job.channel() != null) {
      sendChannelMessage(job.channel(), job.threadTs(), response);
    } else {
      log.warn("Slack job has neither responseUrl nor channel: {}", job);
    }
  }

  private void sendSlashCommandResponse(String responseUrl, String message) {
    try {
      slashCommandResponseSender.send(
          responseUrl, SlashCommandResponse.builder().text(message).build());
    } catch (IOException e) {
      log.error("Failed to send response to Slack", e);
    }
  }

  private void sendChannelMessage(String channel, String threadTs, String message) {
    if (slackBotToken == null || slackBotToken.isBlank()) {
      log.error("SLACK_BOT_TOKEN not set; cannot post channel message");
      return;
    }
    try {
      ChatPostMessageRequest request =
          ChatPostMessageRequest.builder().channel(channel).text(message).build();
      if (threadTs != null && !threadTs.isBlank()) {
        request.setThreadTs(threadTs);
      }
      ChatPostMessageResponse result =
          Slack.getInstance().methods(slackBotToken).chatPostMessage(request);
      if (!result.isOk()) {
        log.error("chat.postMessage failed: {}", result.getError());
      }
    } catch (Exception e) {
      log.error("Failed to post message to channel {}", channel, e);
    }
  }
}
