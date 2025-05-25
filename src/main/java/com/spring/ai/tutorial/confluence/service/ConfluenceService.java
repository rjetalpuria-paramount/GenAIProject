package com.spring.ai.tutorial.confluence.service;

import com.spring.ai.tutorial.confluence.client.ConfluenceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfluenceService {
  private final ConfluenceClient confluenceClient;

  public void fetchConfluencePage(String pageId) {
    log.info("Fetching data from Confluence...");
    try {
      var page = confluenceClient.getPageById(pageId);
      log.info("Fetched {} page from Confluence", page);
    } catch (Exception e) {
      log.error("Error fetching data from Confluence", e);
    }
  }
}
