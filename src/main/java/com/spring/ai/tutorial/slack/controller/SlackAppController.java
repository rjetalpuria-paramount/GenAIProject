package com.spring.ai.tutorial.slack.controller;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;

/**
 * Bolt servlet: receives Slack requests at POST /slack/events (verification + commands + events).
 */
public class SlackAppController extends SlackAppServlet {

  public SlackAppController(App app) {
    super(app);
  }
}
