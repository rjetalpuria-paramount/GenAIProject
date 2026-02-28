package com.spring.ai.tutorial.slack.config;

import com.slack.api.Slack;
import com.slack.api.app_backend.slash_commands.SlashCommandResponseSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

  @Bean
  public SlashCommandResponseSender slashCommandResponseSender() {
    return new SlashCommandResponseSender(new Slack());
  }
}
