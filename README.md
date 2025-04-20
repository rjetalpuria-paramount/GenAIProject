# GenAI Tutorial:

This is a tutorial for using the GenAI library.

**Tutorial Link:** https://www.youtube.com/watch?v=9Crrhz0pm8s

## Local Setup:
1. LM Studio: Download and host LLM models locally.
   - Listens on port 1234.
2. Nginx Proxy: For converting HTTP2 request made by the OpenAI client to HTTP1.1.
    - Listens on port 8081 and forwards requests to LM Studio on port 1234.
3. This Spring Boot application: For serving the GenAI API.
    - Listens on port 8080 and makes calls to the model hosted on LM Studio via Nginx.

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
