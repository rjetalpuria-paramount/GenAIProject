package com.spring.ai.tutorial.chat.service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
  private final ChatModel chatModel;
  private final JdbcTemplate jdbcTemplate;
  private final PgVectorStore vectorStore;
  private ChatClient chatClient;
  private ChatMemory chatMemory;
  private RewriteQueryTransformer rewriteQueryTransformer;
  private OpenAiChatOptions chatOptions;
  private OpenAiChatOptions streamingChatOptions;

  @Value("${com.ai.springAiTutorial.model}")
  private String modelName;

  @Value("${com.ai.springAiTutorial.topP}")
  private Double topP;

  @Value("${com.ai.springAiTutorial.temperature}")
  private Double temperature;

  @Value("${com.ai.springAiTutorial.chatMemorySize}")
  private Integer chatMemorySize;

  private final Predicate<ChatResponse> isResponseValid =
      response ->
          response != null
              && response.getResult() != null
              && response.getResult().getOutput() != null;

  @PostConstruct
  void init() {
    // Initialize ChatMemory with JDBC repository
    // JdbcChatMemoryRepository stores chat messages in a relational database using JDBC
    ChatMemoryRepository chatMemoryRepository =
        JdbcChatMemoryRepository.builder().jdbcTemplate(jdbcTemplate).build();
    chatMemory =
        MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(chatMemorySize)
            .build();
    MessageChatMemoryAdvisor chatMemoryAdvisor =
        MessageChatMemoryAdvisor.builder(chatMemory).build();
    // VectorStoreDocumentRetriever is used to retrieve relevant documents from the vector store
    // based on the similarity to the input query
    VectorStoreDocumentRetriever vectorStoreDocumentRetriever =
        VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.50)
            .topK(5)
            .build();
    // RetrievalAugmentationAdvisor is used to append relevant documents from the vector store to
    // the input query to provide more context to the model
    RetrievalAugmentationAdvisor ragAdvisor =
        RetrievalAugmentationAdvisor.builder()
            .documentRetriever(vectorStoreDocumentRetriever)
            .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
            .build();
    // RewriteQueryTransformer is used to transform the input query before sending it to the model.
    // It uses an LLM to rewrite the query for better context using the chat history.
    rewriteQueryTransformer =
        RewriteQueryTransformer.builder().chatClientBuilder(ChatClient.builder(chatModel)).build();
    // Initialize ChatClient with ChatMemory advisor and RAG advisor
    chatClient =
        ChatClient.builder(chatModel).defaultAdvisors(chatMemoryAdvisor, ragAdvisor).build();

    // Configure OpenAI chat options used to build the LLM API requests
    chatOptions =
        OpenAiChatOptions.builder().model(modelName).topP(topP).temperature(temperature).build();
    streamingChatOptions = OpenAiChatOptions.fromOptions(chatOptions);
    streamingChatOptions.setStreamUsage(true);
  }

  public String getResponse(String message, UUID conversationId) {
    Query query = Query.builder().text(message).history(getChatHistory(conversationId)).build();
    Query transformedQuery = rewriteQueryTransformer.transform(query);

    ChatResponse response =
        chatClient
            .prompt(
                Prompt.builder().chatOptions(chatOptions).content(transformedQuery.text()).build())
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .chatResponse();
    return isResponseValid.test(response) ? response.getResult().getOutput().getText() : null;
  }

  public Flux<String> getStreamingResponse(String message, UUID conversationId) {
    Query query = Query.builder().text(message).history(getChatHistory(conversationId)).build();
    Query transformedQuery = rewriteQueryTransformer.transform(query);

    Flux<ChatResponse> response =
        chatClient
            .prompt(
                Prompt.builder().chatOptions(chatOptions).content(transformedQuery.text()).build())
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .stream()
            .chatResponse();

    return response.mapNotNull(
        resp -> isResponseValid.test(resp) ? resp.getResult().getOutput().getText() : null);
  }

  public List<Message> getChatHistory(UUID conversationId) {
    return conversationId != null
        ? chatMemory.get(conversationId.toString())
        : Collections.emptyList();
  }
}
