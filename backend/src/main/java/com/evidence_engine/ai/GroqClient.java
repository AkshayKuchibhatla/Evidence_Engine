package com.evidence_engine.backend.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Calls Groq's chat API and returns the answer as JSON. */
public class GroqClient {

    private static final URI GROQ_URL = URI.create("https://api.groq.com/openai/v1/chat/completions");

    private final String apiKey;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public GroqClient(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    /** No key = mock mode. */
    public boolean hasKey() {
        return !apiKey.isEmpty();
    }

    /** Sends instructions + message, returns the JSON answer. reasoningEffort: low/medium/high or null. */
    public Map<String, Object> askForJson(String model, String instructions, String message,
                                          String reasoningEffort) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", instructions),
                Map.of("role", "user", "content", message)));
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0.2);
        if (reasoningEffort != null && model.startsWith("openai/gpt-oss")) {
            body.put("reasoning_effort", reasoningEffort);
        }

        HttpRequest request = HttpRequest.newBuilder(GROQ_URL)
                .timeout(Duration.ofSeconds(90))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.write(body)))
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new AiException("Couldn't connect to Groq: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("Stopped while waiting for Groq", e);
        }

        if (response.statusCode() == 401) {
            throw new AiException("Groq rejected the API key (401). Check GROQ_API_KEY.");
        }
        if (response.statusCode() == 429) {
            throw new AiException("Groq rate limit hit (429). Wait a minute and try again.");
        }
        if (response.statusCode() != 200) {
            String text = response.body();
            throw new AiException("Groq error " + response.statusCode() + ": "
                    + text.substring(0, Math.min(300, text.length())));
        }

        // Answer is choices[0].message.content (a JSON string).
        try {
            Map<String, Object> reply = Json.asMap(Json.parse(response.body()));
            Object choice = Json.asList(reply.get("choices")).get(0);
            String content = Json.asString(Json.asMap(Json.asMap(choice).get("message")).get("content"));
            Object answer = Json.parse(content);
            if (!(answer instanceof Map)) {
                throw new AiException("The AI's answer wasn't a JSON object");
            }
            return Json.asMap(answer);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new AiException("The AI didn't send back valid JSON", e);
        }
    }
}
