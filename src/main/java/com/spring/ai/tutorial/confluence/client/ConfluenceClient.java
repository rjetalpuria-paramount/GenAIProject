package com.spring.ai.tutorial.confluence.client;

import com.spring.ai.tutorial.confluence.model.ConfluenceDocument;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "confluenceClient",
    url = "${confluence.baseUrl}",
    path = "/wiki/rest/api",
    configuration = ConfluenceClientConfig.class)
public interface ConfluenceClient {

  @GetMapping("/content?spaceKey=${confluence.spaceKey}")
  List<Object> getAllPagesInSpace();

  @GetMapping("/content/{pageId}?expand=body.view,version")
  ConfluenceDocument getPageById(@PathVariable String pageId);
}
