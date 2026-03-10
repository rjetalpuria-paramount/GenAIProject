package com.spring.ai.tutorial.slack.model;

/**
 * Payload for enqueueing a Slack job (slash command or app_mention). Used by {@link
 * com.spring.ai.tutorial.slack.service.SlackJobService}.
 *
 * <p>Slash command: {@code responseUrl} set; {@code channel} and {@code threadTs} null. App
 * mention: {@code channel} and {@code threadTs} set; {@code responseUrl} null.
 */
public record SlackJobPayload(String text, String responseUrl, String channel, String threadTs) {

  /** For slash commands: text and response URL only. */
  public static SlackJobPayload slashCommand(String text, String responseUrl) {
    return new SlackJobPayload(text, responseUrl, null, null);
  }

  /** For app_mention: text, channel, and optional thread ts. */
  public static SlackJobPayload appMention(String text, String channel, String threadTs) {
    return new SlackJobPayload(text, null, channel, threadTs);
  }
}
