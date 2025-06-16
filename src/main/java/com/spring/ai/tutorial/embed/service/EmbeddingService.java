package com.spring.ai.tutorial.embed.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmbeddingService {
  private final PgVectorStore vectorStore;

  public void generateAndPersistEmbeddings(List<Document> document) {
    List<Document> chunks = splitDocumentIntoChunks(document);
    vectorStore.add(chunks);
  }

  private List<Document> splitDocumentIntoChunks(List<Document> document) {
    TokenTextSplitter splitter = new TokenTextSplitter();
    return splitter.apply(document);
  }
}
