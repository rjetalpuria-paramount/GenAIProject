# GenAI Tutorial:

This is a tutorial for using the GenAI library.

**Tutorial Link:** https://www.youtube.com/watch?v=9Crrhz0pm8s

## Principles:
1. **No Paid APIs or Tools**: Focus on using open-source tools and libraries.
   - The goal is to create a self-hosted solution that does not rely on paid APIs or tools. Down the line, if needed, we can switch to paid APIs without major code changes.
2. **OpenAI API Compatibility**:
   - The solution should be compatible with OpenAI API, allowing for easy integration with existing OpenAI clients.
   - This is achieved by using LM Studio, which provides OpenAI API compatible endpoints.
3. **Local Hosting**:
   - The solution should be hosted locally, allowing for full control over the environment and data.
   - This is achieved by using LM Studio for LLM models, Nginx for HTTP2 to HTTP1.1 conversion, and PostgreSQL for chat history storage.

## Local Setup:
1. A checkout of this repo
    - Listens on port 8080 and calls the model hosted on LM Studio via Nginx.
2. LM Studio: Download and host LLM models locally.
   - Listens on port 1234 (default)
3. Nginx Proxy: For converting HTTP2 request made by the OpenAI client to HTTP1.1.
    - Listens on port 8081 and forwards requests to LM Studio on port 1234.
    - Run the following in the terminal:
    ```bash
    docker-compose -f ./nginx/docker-compose.yaml up -d
    ```
4. PostgreSQL: Database for storing chat data.
    - Listens on port 8082
    - Run the following in the terminal:
    ```bash
    docker-compose -f ./postgres/docker-compose.yaml up -d
    ```
   - Make sure the specify the following environment variables for setting up database connection:
    ```yaml
    `DB_URL` # JDBC URL e.g. jdbc:postgresql://localhost:5432/my_database
    `DB_USERNAME` # username
    `DB_PASSWORD` # password
    ```
   - The project uses the DB to store chat history via Spring's JdbcChatMemory, and Spring expects `ai_chat_memory` table to be present in the database.
     - Run the following SQL query to setup the table:
       ```sql
       CREATE TABLE ai_chat_memory (
         "id" SERIAL NOT NULL,
         "conversation_id" VARCHAR(40),
         "content" TEXT NOT NULL,
         "type" VARCHAR(10) NOT NULL,
         "timestamp" TIMESTAMP NOT NULL DEFAULT NOW(),
         PRIMARY KEY (id)
       );
       CREATE INDEX idx_memory_conversation_id ON ai_chat_memory ("conversation_id");
       ```
     - Run the following SQL query to setup the table and index for vector store: ([source](https://docs.spring.io/spring-ai/reference/1.0/api/vectordbs/pgvector.html#_prerequisites))
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
5. Atlassian PAT (Personal Access Token)
   - Navigate [here](https://id.atlassian.com/manage-profile/security/api-tokens) and create a new token
   - Once created, base64 encode your email and token: `<your_email>:<your_token>`
     ```bash
     echo -n your_email:your_token | base64
     ```
   - Save the encoded result in an enviroment variable called `ATL_TOKEN`

## Problems Faced:
### Problem: OpenAI API is paid and I didn't want to pay for it.
#### Solution:
- Host the model locally using LM Studio.
- It provides OpenAI API compatible endpoints, so in future, I can switch to OpenAI API without changing the code.
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
- Create an uuid for each conversation and store the chat history in a PostgreSQL database.
- Improvement (not yet implemented): Use vector store for storing chat history and retrieving relevant context for the conversation.
### Problem: Figuring out accessing Confluence documents.
#### Solution:
- Use Atlassian's Confluence REST API to fetch the knowledge base.
- Use the Atlassian PAT (Personal Access Token) for authentication.
### Problem: Figuring out creating embeddings from the Confluence pages.
#### Subproblem: Confluence pages are in HTML format, which also have styling and other non-semantic content.
##### Solution:
- Use Jsoup to parse the HTML and extract the text content.
#### Subproblem: Figuring out the optimal chunk size for creating embeddings.
##### Solution:
- TBD
### Subproblem: Figuring out embedding strategy (ex: BM25, etc.)
##### Solution:
- TBD
