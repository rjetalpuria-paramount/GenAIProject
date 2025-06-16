package com.spring.ai.tutorial.confluence.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceDocument {
  @Getter
  @JsonProperty("id")
  private String id;

  @Getter
  @JsonProperty("title")
  private String title;

  @JsonProperty("body")
  private DocBody body;

  @JsonProperty("_links")
  private Links links;

  private static class DocBody {
    @JsonProperty("view")
    private View view;
  }

  private static class View {
    @JsonProperty("value")
    private String value;
  }

  private static class Links {
    @JsonProperty("self")
    private String selfLink;
  }

  public String getContent() {
    return this.body.view.value;
  }

  public String getSelfLink() {
    return this.links.selfLink;
  }
}
