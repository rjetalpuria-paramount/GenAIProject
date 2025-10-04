package com.spring.ai.tutorial.confluence.service;

import com.spring.ai.tutorial.confluence.client.ConfluenceClient;
import com.spring.ai.tutorial.confluence.model.ConfluenceDocument;
import com.spring.ai.tutorial.confluence.model.ConfluenceDocumentPage;
import com.spring.ai.tutorial.embed.service.EmbeddingService;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfluenceService {
  private static final int CONFLUENCE_PAGE_SIZE = 2;
  private final ConfluenceClient confluenceClient;
  private final EmbeddingService embeddingService;

  public void embedConfluenceDocumentById(String documentId) {
    ConfluenceDocument confluenceDocument = confluenceClient.getPageById(documentId);
    processConfluenceDocument(confluenceDocument);
  }

  public void embedAllConfluenceDocuments() {
    int startIndex = 0;
    ConfluenceDocumentPage page;

    do {
      page = confluenceClient.getAllPagesInSpace(startIndex, CONFLUENCE_PAGE_SIZE);
      page.getResults().forEach(this::processConfluenceDocument);

      startIndex = startIndex + CONFLUENCE_PAGE_SIZE;
    } while (page.getSize() == CONFLUENCE_PAGE_SIZE);
  }

  private void processConfluenceDocument(ConfluenceDocument confluenceDocument) {
    log.info("Processing Confluence document: {}", confluenceDocument.getTitle());
    String sanitizedHtmlContent = sanitizeHtmlContent(confluenceDocument.getContent());
    String markdownContent = convertHtmlToMarkdown(sanitizedHtmlContent);
    Map<String, Object> additionalMetadataMap =
        Map.of(
            "docId", confluenceDocument.getId(),
            "docTitle", confluenceDocument.getTitle(),
            "link", confluenceDocument.getSelfLink());
    List<Document> documents = createDocumentsFromMarkdown(markdownContent, additionalMetadataMap);
    embeddingService.generateAndPersistEmbeddings(documents);
  }

  @Deprecated(forRemoval = true)
  private List<Document> extractContent(ConfluenceDocument confluenceDocument) {
    Map<String, Object> additionalMetadataMap =
        Map.of(
            "docId", confluenceDocument.getId(),
            "title", confluenceDocument.getTitle(),
            "link", confluenceDocument.getSelfLink());

    JsoupDocumentReaderConfig config =
        JsoupDocumentReaderConfig.builder()
            .selector("h1, h2, h3, h4, h5, h6, p, li, th, td, a, img, blockquote")
            .includeLinkUrls(true)
            .groupByElement(false)
            .additionalMetadata(additionalMetadataMap)
            .build();

    Resource resource =
        new ByteArrayResource(confluenceDocument.getContent().getBytes(StandardCharsets.UTF_8));
    JsoupDocumentReader reader = new JsoupDocumentReader(resource, config);
    return reader.get();
  }

  private String sanitizeHtmlContent(String htmlContent) {
    Safelist safelist =
        new Safelist()
            .addTags(
                "h1",
                "h2",
                "h3",
                "h4",
                "h5",
                "h6",
                "p",
                "strong",
                "em",
                "ul",
                "ol",
                "li",
                "table",
                "thead",
                "tbody",
                "tr",
                "th",
                "td",
                "blockquote",
                "a")
            .addAttributes("a", "href")
            .removeTags();
    return Jsoup.clean(htmlContent, safelist);
  }

  private String convertHtmlToMarkdown(String cleanedHtml) {
    FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
    return converter.convert(cleanedHtml);
  }

  private List<Document> createDocumentsFromMarkdown(
      String markdownContent, Map<String, Object> additionalMetadataMap) {
    MarkdownDocumentReaderConfig config =
        MarkdownDocumentReaderConfig.builder()
            .withAdditionalMetadata(additionalMetadataMap)
            .withHorizontalRuleCreateDocument(true)
            .withIncludeCodeBlock(true)
            .withIncludeBlockquote(true)
            .build();
    Resource resource = new ByteArrayResource(markdownContent.getBytes(StandardCharsets.UTF_8));
    MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
    return reader.get();
  }
}
