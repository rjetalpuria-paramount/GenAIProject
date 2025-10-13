package com.spring.ai.tutorial.embed.confluence.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceDocumentPage {
  @Getter
  @JsonProperty("results")
  List<ConfluenceDocument> results;

  @Getter
  @JsonProperty("start")
  int start;

  @Getter
  @JsonProperty("limit")
  int limit;

  @Getter
  @JsonProperty("size")
  int size;
}
