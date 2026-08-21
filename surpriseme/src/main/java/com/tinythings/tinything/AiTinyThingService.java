package com.tinythings.tinything;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class AiTinyThingService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public AiTinyThingService(
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.model}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
    }

    public record GeneratedThing(
            String title,
            String description,
            String category,
            List<String> tags
    ) {}

    public Optional<GeneratedThing> generate(
            String role,
            String field,
            List<String> interests,
            String focusAreas,
            String goalsText
    ) {

        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        String prompt = buildPrompt(role, field, interests, focusAreas, goalsText, pickCategory());

        try {

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.8,
                    "max_tokens", 1000
            );

            String response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            System.out.println("========== GROQ RESPONSE ==========");
            System.out.println(response);
            System.out.println("===================================");

            return parseResponse(response);

        } catch (Exception e) {
            System.err.println("Groq generation failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Explicitly rotates categories so gratitude/hydration/goal/general/interest
     * all get fair representation, instead of leaving it to the AI's judgment
     * (which tends to drift toward whatever the profile's interests suggest).
     */
    private String pickCategory() {
        int roll = random.nextInt(100);
        if (roll < 25) return "gratitude";       // guaranteed 25% of the time
        if (roll < 40) return "hydration-adjacent"; // light nudge, not literal water logging
        if (roll < 55) return "goal";
        if (roll < 70) return "general";
        return "interest";                        // remaining chance: pull from user's interests
    }

    private String buildPrompt(
            String role,
            String field,
            List<String> interests,
            String focusAreas,
            String goalsText,
            String category
    ) {

        String interestsList = interests != null && !interests.isEmpty()
                ? String.join(", ", interests)
                : "general wellbeing";

        String categoryInstruction = switch (category) {
            case "gratitude" -> "Generate a GRATITUDE action: something about noticing, appreciating, or expressing thanks for something small. Category must be \"gratitude\".";
            case "hydration-adjacent" -> "Generate a simple PHYSICAL/WELLNESS action unrelated to any specific skill - like a stretch, a breath, or drinking water. Category must be \"general\".";
            case "goal" -> "Generate a small PRODUCTIVITY action that helps make progress on something, unrelated to any specific skill. Category must be \"goal\".";
            case "general" -> "Generate a simple, universal action anyone could do, regardless of interests. Category must be \"general\".";
            default -> "Generate an action based specifically on ONE of the user's interests below, picked at random. Category must be \"general\".";
        };

        return """
            You generate tiny, delightful 1-5 minute actions
            for an app called "Tiny Things".

            %s

            IMPORTANT - keep it SIMPLE:
            - No jargon, no technical terms, no special skills or setup required
            - Something a complete beginner could do immediately, in plain everyday language
            - Avoid anything that requires prior knowledge, tools, or expertise
            - Think "step outside for a second" not "refactor a function"
            - Give Emotional touch to it, so that it can bring a smile on user's face whenever he/she sees it

            User context (use only if directly relevant to the instruction above):
            - Role: %s
            - Field: %s
            - Interests: %s
            - Focus areas: %s
            - Long-term goal: %s

            Respond with ONLY the raw JSON object below. No markdown, no code fences, no explanation, no extra text before or after. Just the JSON object itself, starting with { and ending with }.

            {
              "title": "short punchy title, max 6 words, plain language",
              "description": "one simple encouraging sentence, max 20 words",
              "category": "hydration|gratitude|goal|general",
              "tags": ["1-3 lowercase tags"]
            }
            """.formatted(
                categoryInstruction,
                role != null ? role : "unspecified",
                field != null ? field : "unspecified",
                interestsList,
                focusAreas != null ? focusAreas : "none specified",
                goalsText != null ? goalsText : "none specified"
        );
    }

    private Optional<GeneratedThing> parseResponse(String rawResponse) {
        System.out.println("Groq model: " + model);
        System.out.println("Groq API key present: " + (apiKey != null && !apiKey.isBlank()));
        try {

            JsonNode root = objectMapper.readTree(rawResponse);

            String text = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            String cleaned = cleanJsonText(text);

            JsonNode parsed = objectMapper.readTree(cleaned);

            String title = parsed.path("title").asText();
            String description = parsed.path("description").asText();
            String category = parsed.path("category").asText("general");

            List<String> tags = new ArrayList<>();
            parsed.path("tags").forEach(tag -> tags.add(tag.asText().toLowerCase()));

            if (title.isBlank() || description.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new GeneratedThing(title, description, category, tags));

        } catch (Exception e) {
            System.err.println("========== GROQ ERROR ==========");
            e.printStackTrace();
//            System.err.println("Failed to parse Groq response: " + e.getMessage());
//            System.err.println("=== RAW RESPONSE ===");
//            System.err.println(rawResponse);
//            System.err.println("====================");
            return Optional.empty();
        }
    }

    private String cleanJsonText(String text) {
        String cleaned = text.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(json)?", "").trim();
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }
        }

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned;
    }
}