package com.spring.ai.tutorial.slack.config;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;
import com.slack.api.model.event.AppMentionEvent;
import com.spring.ai.tutorial.slack.controller.SlackAppController;
import com.spring.ai.tutorial.slack.model.SlackJobPayload;
import com.spring.ai.tutorial.slack.service.SlackJobService;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Slack Bolt app. Credentials are read from environment variables SLACK_BOT_TOKEN
 * and SLACK_SIGNING_SECRET (see https://api.slack.com/apps).
 */
@Configuration
public class BoltSlackAppConfig {

  @Bean
  public App initSlackApp(SlackJobService slackJobService) {
    App app = new App();
    app.command(
        "/gen-ai-help",
        (req, ctx) -> {
          slackJobService.addJob(
              SlackJobPayload.slashCommand(
                  req.getPayload().getText(), req.getPayload().getResponseUrl()));
          return ctx.ack("Your request is being processed. You will receive a response shortly.");
        });

    app.event(
        AppMentionEvent.class,
        (payload, ctx) -> {
          AppMentionEvent event = payload.getEvent();
          String channel = event.getChannel();
          String threadTs = event.getTs();
          String text = event.getText();
          if (text != null) {
            text = text.replaceFirst("^<@\\w+>\\s*", "").trim();
          } else {
            text = "";
          }

          slackJobService.addJob(SlackJobPayload.appMention(text, channel, threadTs));

          ctx.client()
              .chatPostMessage(
                  r ->
                      r.channel(channel)
                          .threadTs(threadTs)
                          .text(
                              "Your request is being processed. You will receive a response"
                                  + " shortly."));

          return ctx.ack();
        });
    return app;
  }

  @Bean
  public ServletRegistrationBean<SlackAppServlet> slackAppServlet(App app) {
    return new ServletRegistrationBean<>(new SlackAppController(app), "/slack/events");
  }
}
