package com.spring.ai.tutorial.embed.confluence.client;

import com.spring.ai.tutorial.embed.confluence.model.ConfluenceDocument;
import com.spring.ai.tutorial.embed.confluence.model.ConfluenceDocumentPage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "confluenceClient",
    url = "${confluence.baseUrl}",
    path = "/wiki/rest/api",
    configuration = ConfluenceClientConfig.class)
public interface ConfluenceClient {

  @GetMapping("/content?spaceKey=${confluence.spaceKey}&expand=body.view,version")
  ConfluenceDocumentPage getAllPagesInSpace(
      @RequestParam(value = "start") int startIndex, @RequestParam(value = "limit") int pageSize);

  @GetMapping("/content/{pageId}?expand=body.view,version")
  ConfluenceDocument getPageById(@PathVariable String pageId);
}
