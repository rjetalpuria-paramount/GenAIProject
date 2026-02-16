package com.spring.ai.tutorial.slack.service;

import com.slack.api.app_backend.slash_commands.SlashCommandResponseSender;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.spring.ai.tutorial.chat.service.ChatService;
import java.io.IOException;
import java.util.Queue;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackJobService {
  private final ChatService chatService;
  private final SlashCommandResponseSender slashCommandResponseSender;

  Queue<SlashCommandPayload> jobQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

  public void addJob(SlashCommandPayload job) {
    jobQueue.offer(job);
  }

  public void processNextJob() {
    SlashCommandPayload job = jobQueue.poll();
    if (job == null) {
      return;
    }
    UUID chatId = UUID.randomUUID();
    String response = chatService.getResponse(job.getText(), chatId);

    sendSlackCommandResponse(job, response);
  }

  private void sendSlackCommandResponse(SlashCommandPayload job, String message) {
    try {
      slashCommandResponseSender.send(
          job.getResponseUrl(), SlashCommandResponse.builder().text(message).build());
    } catch (IOException e) {
      log.error("Failed to send response to Slack", e);
    }
  }
}
