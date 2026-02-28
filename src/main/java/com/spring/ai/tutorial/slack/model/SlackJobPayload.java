package com.spring.ai.tutorial.slack.model;

/**
 * Minimal payload for enqueueing a Slack slash-command job (e.g. from Bolt or legacy controller).
 * Used by {@link com.spring.ai.tutorial.slack.service.SlackJobService}.
 */
public record SlackJobPayload(String text, String responseUrl) {}
