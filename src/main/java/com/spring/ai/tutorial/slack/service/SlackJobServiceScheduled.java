package com.spring.ai.tutorial.slack.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackJobServiceScheduled {
  private final SlackJobService slackJobService;

  @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
  public void processJobs() {
    slackJobService.processNextJob();
  }
}
