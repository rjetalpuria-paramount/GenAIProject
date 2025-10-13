package com.spring.ai.tutorial.embed.confluence.service;

import static com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter.HEADING_NODES;

import com.spring.ai.tutorial.embed.confluence.client.ConfluenceClient;
import com.spring.ai.tutorial.embed.confluence.model.ConfluenceDocument;
import com.spring.ai.tutorial.embed.service.EmbeddingService;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfluenceEmbeddingService implements EmbeddingService<ConfluenceDocument> {
  private final ChatModel chatModel;
  private final PgVectorStore vectorStore;
  private final ConfluenceClient confluenceClient;

  @Override
  public PgVectorStore getVectorStore() {
    return vectorStore;
  }

  @Override
  public ConfluenceDocument getDocumentsById(String documentId) {
    return confluenceClient.getPageById(documentId);
  }

  @Override
  public List<ConfluenceDocument> getDocumentsByPage(int startIndex, int pageSize) {
    return confluenceClient.getAllPagesInSpace(startIndex, pageSize).getResults();
  }

  @Override
  public List<Document> convertToDocuments(ConfluenceDocument confluenceDocument) {

    log.info("Processing Confluence document: {}", confluenceDocument.getTitle());
    String sanitizedHtmlContent = sanitizeHtmlContent(confluenceDocument.getContent());
    String markdownContent = convertHtmlToMarkdown(sanitizedHtmlContent);
    Map<String, Object> additionalMetadataMap =
        Map.of(
            "docId", confluenceDocument.getId(),
            "docTitle", confluenceDocument.getTitle(),
            "link", confluenceDocument.getSelfLink());
    List<Document> documents = createDocumentsFromMarkdown(markdownContent, additionalMetadataMap);
    documents = attachKeywords(documents);
    return documents;
  }

  private String sanitizeHtmlContent(String htmlContent) {
    Safelist safelist = Safelist.basic().addTags(HEADING_NODES);
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

  private List<Document> attachKeywords(List<Document> documents) {
    KeywordMetadataEnricher keywordMetadataEnricher =
        KeywordMetadataEnricher.builder(chatModel).keywordCount(10).build();
    return keywordMetadataEnricher.apply(documents);
  }
}
