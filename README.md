<img src="docsy-logo.png" alt="docsy-logo" style="width:128px"/>

# Docsy: Slackbot powered by GenAI!

Slack bot that answers questions based on internal documentation using a self-hosted RAG system built with Spring Boot and Spring AI, integrating with Confluence for document retrieval, and leveraging open-source LLMs via OpenAI-compatible APIs.

## Principles:
1. **No Paid APIs or Tools**: Focus on using open-source tools and libraries.
   - The goal is to create a self-hosted solution that does not rely on paid APIs or tools. Down the line, if needed, we can switch to paid APIs without major code changes.
2. **OpenAI API Compatibility**:
   - The solution should be compatible with the OpenAI API, allowing for easy integration with existing OpenAI clients.
   - This is achieved by using LM Studio, which provides OpenAI API compatible endpoints.
3. **Local Hosting**:
   - The solution should be hosted locally, allowing for full control over the environment and data.
   - This is achieved by using LM Studio for LLM models, Nginx for HTTP2 to HTTP1.1 conversion, and PostgreSQL for chat history storage.

## Architecture Diagram:
![architecture-diagram.png](architecture-diagram.png)

## Local Setup:
1. A checkout of this repo
    - Listens on port 8080 and calls the model hosted on LM Studio via Nginx.
2. LM Studio: Download and host LLM models locally.
   - Listens on port 1234 (default)
   - Models used:
     - [nomic-ai/nomic-embed-text-v1.5-GGUF](https://huggingface.co/nomic-ai/nomic-embed-text-v1.5-GGUF)
     - [google/gemma-3-12b](https://lmstudio.ai/models/google/gemma-3-12b)
3. Docker: For hosting Nginx and PostgreSQL
  ```bash
  docker-compose -f ./docker/docker-compose.yaml up -d
  ```
  - **Nginx Proxy:** For converting HTTP2 request made by the OpenAI client to HTTP1.1.
    - Listens on port 8081 and forwards requests to LM Studio on port 1234.
    - Ensure the nginx.conf file has the correct LM Studio host IP Address and port configuration.
  - **PostgreSQL:** Database for storing chat data.
    - Listens on port 8082
    - Make sure the specify the following environment variables for setting up the database connection:
     ```yaml
     DB_URL # JDBC URL e.g. jdbc:postgresql://localhost:5432/my_database
     DB_USERNAME # username
     DB_PASSWORD # password
     ```
    - The project uses the DB to store chat history via Spring's JdbcChatMemory, and Spring expects `spring_ai_chat_memory` table to be present in the database.
      - Run the following SQL query to set up the table:
        ```sql
        CREATE TABLE spring_ai_chat_memory (
          "id" SERIAL NOT NULL,
          "conversation_id" VARCHAR(40),
          "content" TEXT NOT NULL,
          "type" VARCHAR(10) NOT NULL,
          "timestamp" TIMESTAMP NOT NULL DEFAULT NOW(),
          PRIMARY KEY (id)
        );
        CREATE INDEX idx_memory_conversation_id ON ai_chat_memory ("conversation_id");
        ```
      - Run the following SQL query to set up the table and index for vector store: ([source](https://docs.spring.io/spring-ai/reference/1.0/api/vectordbs/pgvector.html#_prerequisites))
        ```sql
        CREATE EXTENSION IF NOT EXISTS vector;
        CREATE EXTENSION IF NOT EXISTS hstore;
        CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
        CREATE TABLE IF NOT EXISTS vector_store (
        id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
        content text,
        metadata json,
        embedding vector(768) -- 1536 is the default embedding dimension
        );
        CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
        ```
  - **Cloudflare Tunnel**: For exposing the Spring Boot backend to the internet for testing Slack integration.
    - Ensure the docker-compose.yaml file has the correct IP Address and port configuration of the Spring Boot backend.
4. Atlassian PAT (Personal Access Token) for Confluence API access:
   - Navigate [here](https://id.atlassian.com/manage-profile/security/api-tokens) and create a new token
   - Once created, base64 encode your email and token: `<your_email>:<your_token>`
     ```bash
     echo -n your_email:your_token | base64
     ```
   - Save the encoded result in an environment variable called `ATL_TOKEN`
     ```yaml
     ATL_TOKEN # base64 encoded string of <your_email>:<your_token>
     ```
   - Also specify the Confluence base URL and space key as environment variables:
     ```yaml
     CONFLUENCE_BASE_URL # e.g. https://your-domain.atlassian.net/wiki
     CONFLUENCE_SPACE_KEY # e.g. MYSPACE
     ```
   - Ref: [Confluence REST API Documentation](https://docs.atlassian.com/atlassian-confluence/REST/6.6.0/)
5. Slack Integration for Chatbot:
    - Provision a new Slack sandbox workspace for testing the chatbot integration: https://api.slack.com/developer-program/dashboard. If you have an actual workspace to test, you may use that instead.
    - Create a new Slack app here: https://api.slack.com/apps, and add the bot to your slack workspace.
    - From the Slack app control panel, get the signing secret and bot user's OAuth token and save them as environment variables:
      ```yaml
      SLACK_SIGNING_SECRET # e.g. 1234XXXXXXXXXXXXXXXXXXXXXXXXX
      SLACK_BOT_TOKEN # e.g. xoxb-xxxxxxxxxxxx-xxxxxxxxxxxx-xxxxxxxxxxxx
      ```
    - Set up an event subscription in your Slack app to listen for messages in channels or direct messages where the bot is mentioned.
      - Add the Bot permissions: OAuth & Permissions > Bot Token Scopes > Add the following scopes:
        - `app_mentions:read` (to read messages where the bot is mentioned)
        - `assistants:write` (to allow the bot to send messages in threads)
        - `chat:write` (to allow the bot to send messages)
        - `incoming-webhook` (to allow the bot to receive messages via webhooks)
      - Subscribe to bot events: Event Subscriptions > Subscribe to Bot Events > Add the following events:
        - `app_mention` (to receive events when the bot is mentioned in channels)
      - Set up the Request URL for event subscriptions to point to your Spring Boot backend exposed via Cloudflare Tunnel, e.g. `https://your-tunnel-url.com/api/slack/events`
        - Check the cloud-flare tunnel logs to get the correct URL.

## Problems Faced:
### Problem: OpenAI API is paid, and I didn't want to pay for it.
#### Solution:
- Host the model locally using LM Studio.
- It provides OpenAI API compatible endpoints, so in the future, I can switch to OpenAI API without changing the code.
- Provides access to various open-source models like Llama2, Mistral, etc.
### Problem: LM Studio doesn't support image generation.
#### Solution:
- Not needed for now -- we cross that bridge when we come to it.
#### Alternatives:
- Use Stable Diffusion for image generation. (Can use LM Studio for generating prompts for Stable Diffusion)
- Stable Diffusion can be hosted locally using ComfyUI or Automatic1111.
### Problem: LM Studio doesn't support HTTP2 and OpenAI client strictly uses HTTP2.
#### Solution:
- Use Nginx as a reverse proxy to convert HTTP2 requests to HTTP1.1.
- Genius idea from [the spring ai Github forum](https://github.com/spring-projects/spring-ai/issues/2441)
- Nginx listens on port 8081 and forwards requests to LM Studio on port 1234.
### Problem: Figuring out chat history
#### Solution:
- Spring AI provides JdbcChatMemory for storing chat history in a database.
- Create a UUID for each conversation and store the chat history in a PostgreSQL database.
- Improvement (not yet implemented): Use a vector store for storing chat history and retrieving relevant context for the conversation.
### Problem: Figuring out how to access Confluence documents.
#### Solution:
- Use Atlassian's Confluence REST API to fetch the knowledge base.
- Use the Atlassian PAT (Personal Access Token) for authentication.
### Problem: Figuring out how to create embeddings from the Confluence pages.
#### Details:
- Confluence pages are in HTML format, which also has styling and other non-semantic content.
- Using Jsoup HTML parser directly on Confluence's HTML causes duplicated content due to the various HTML tags (especially for table formatting tags)
##### Solution:
- Sanitize the HTML content using Jsoup - this removes styling and other tags
- Convert it into Markdown - provides cleaner structure, and concise formatting (aka reduces token count)
- Create Documents from converted Markdown
#### Subproblem: Figuring out the optimal chunk size for creating embeddings.
##### Solution:
- TBD
### Subproblem: Figuring out embedding strategy (ex, BM25, etc.)
##### Solution:
- TBD
