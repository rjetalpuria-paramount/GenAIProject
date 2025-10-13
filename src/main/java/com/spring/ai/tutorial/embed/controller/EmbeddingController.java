package com.spring.ai.tutorial.embed.controller;

import com.spring.ai.tutorial.embed.confluence.service.ConfluenceEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/embed")
@RequiredArgsConstructor
public class EmbeddingController {
  private final ConfluenceEmbeddingService confluenceEmbeddingService;

  @GetMapping("/confluence")
  public ResponseEntity<Void> embedConfluencePages(
      @RequestParam(required = false) String documentId) {
    if (StringUtils.isNotBlank(documentId)) {
      confluenceEmbeddingService.embedById(documentId);
    } else {
      confluenceEmbeddingService.embedAll();
    }
    return ResponseEntity.ok().build();
  }
}
