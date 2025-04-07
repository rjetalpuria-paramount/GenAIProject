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

Okay, let's break down \"top-k\" and \"top-p\" (also known as \"nucleus sampling\") – these are common techniques used in generative AI models like large language models (LLMs) such as GPT-3, LLaMA, Gemini, etc. They control how the model chooses its next word/token when generating text.  They're ways to make the generation more creative and less predictable than just picking the *most* likely word every time.\n\n**1. Top-K Sampling**\n\n*   **The Idea:**  Imagine the AI is trying to decide what the next word should be in a sentence. It has calculated probabilities for *every single possible word* (or token, we'll explain tokens later) in its vocabulary.  Top-k sampling says: \"Let's only consider the 'K' most likely words.\"\n\n*   **How it Works:**\n    *   The model calculates a probability distribution over all potential next tokens (words or sub-word units).\n    *   You set a value for 'K' (e.g., K=10, K=50).\n    *   The model then selects the top 'K' most probable words from that distribution.\n    *   It *renormalizes* those 'K' probabilities so they add up to 1. This is important because we now only consider a subset of the original possibilities.  (Without renormalization, it would be hard to randomly choose one).\n    *   The next word is then selected randomly from these 'K' words, according to their renormalized probabilities.\n\n*   **Example:** Let’s say K=5 and the model has calculated:\n    1. \"the\": 0.30  (most likely)\n    2. \"a\": 0.25\n    3. \"of\": 0.18\n    4. \"in\": 0.12\n    5. \"to\": 0.09 (fifth most likely)\n    6. All other words have probabilities below 0.09\n\n    Top-K sampling would only consider “the”, “a”, “of”, “in”, and “to”.  It would then renormalize those probabilities to add up to one, and randomly select a word from that smaller set.\n\n*   **Pros:**\n    *   Reduces the chance of very unlikely or nonsensical words being generated.\n    *   Simple to implement.\n*   **Cons:**\n    *   'K' is a fixed value. It doesn’t adapt based on the probability distribution itself. If the top few options are *very* similar in probability, Top-K can still be too restrictive.  Conversely, if one word is overwhelmingly more likely than all others, it might not add much benefit.\n    *   Can sometimes lead to repetitive or predictable text if 'K' is too large.\n\n**2. Top-P (Nucleus Sampling)**\n\n*   **The Idea:**  Top-p sampling addresses the limitations of top-k by dynamically adjusting the number of words considered based on their cumulative probability. It’s a more sophisticated approach that aims for a balance between creativity and coherence. Instead of choosing the *top K* options, it chooses the smallest set of options whose probabilities add up to at least 'P'.\n\n*   **How it Works:**\n    *   The model calculates a probability distribution over all possible next tokens.\n    *   You set a value for 'P' (e.g., P=0.75, P=0.9). This is often referred to as the \"nucleus size\".\n    *   Sort the potential words by their probabilities in descending order.\n    *   Start adding up the probabilities of these sorted tokens *from the most likely one*.\n    *   Stop when the cumulative probability reaches or exceeds 'P'.  The set of words you've accumulated are now your \"nucleus.\"\n    *   Renormalize the probabilities within this nucleus so they sum to 1.\n    *   Choose a word randomly from the nucleus according to its renormalized probability.\n\n*   **Example:** Let’s say P=0.8 and the model has calculated:\n    1. “the”: 0.35\n    2. “a”: 0.25\n    3.
