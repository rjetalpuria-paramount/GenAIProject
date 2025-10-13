package com.spring.ai.tutorial.embed.service;

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.util.CollectionUtils;

public interface EmbeddingService<T> {
  TokenTextSplitter splitter = new TokenTextSplitter();

  PgVectorStore getVectorStore();

  T getDocumentsById(String documentId);

  List<T> getDocumentsByPage(int startIndex, int pageSize);

  List<Document> convertToDocuments(T rawDocument);

  default void embedAll() {
    int startIndex = 0;
    int pageSize = 10;

    List<T> rawDocuments;
    do {
      rawDocuments = getDocumentsByPage(startIndex, pageSize);
      if (CollectionUtils.isEmpty(rawDocuments)) break;

      rawDocuments.forEach(this::generateEmbeddings);
      startIndex += pageSize;
    } while (rawDocuments.size() >= pageSize);
  }

  default void embedById(String documentId) {
    T rawDocument = getDocumentsById(documentId);
    if (rawDocument == null) return;
    generateEmbeddings(rawDocument);
  }

  private void generateEmbeddings(T rawDocument) {
    List<Document> documents = convertToDocuments(rawDocument);
    List<Document> chunks = splitter.apply(documents);
    getVectorStore().add(chunks);
  }
}
