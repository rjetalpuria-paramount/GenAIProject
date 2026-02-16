package com.spring.ai.tutorial.slack.controller;

import com.slack.api.app_backend.slash_commands.SlashCommandPayloadParser;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.spring.ai.tutorial.slack.service.SlackJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/slack")
@RequiredArgsConstructor
public class SlackController {
  private final SlackJobService slackJobService;
  private final SlashCommandPayloadParser slashCommandPayloadParser;

  @PostMapping("/help")
  public ResponseEntity<String> getChatResponse(@RequestBody String payload) {
    SlashCommandPayload slashCommandPayload = slashCommandPayloadParser.parse(payload);
    slackJobService.addJob(slashCommandPayload);

    return ResponseEntity.ok(
        "Your request is being processed. You will receive a response shortly.");
  }
}
