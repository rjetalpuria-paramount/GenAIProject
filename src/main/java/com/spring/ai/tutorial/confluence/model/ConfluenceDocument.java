package com.spring.ai.tutorial.confluence.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceDocument {
  @JsonProperty("id")
  private String id;

  @JsonProperty("title")
  private String title;

  @JsonProperty("body")
  private DocBody body;

  public static class DocBody {
    @JsonProperty("view")
    private View view;
  }

  public static class View {
    @JsonProperty("value")
    private String value;
  }

  public String getContent() {
    return this.body.view.value;
  }
}
