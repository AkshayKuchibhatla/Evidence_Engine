package com.evidence_engine.backend.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Step 1: thesis + reason -> search terms (small fast model). Code builds the query. */
public class QueryPlanner {

    public static final String DEFAULT_MODEL = "openai/gpt-oss-20b";

    private static final String INSTRUCTIONS = """
            You help college students search scholarly library databases.
            You will get an essay thesis and one reason that supports it.

            Find the 2-4 main ideas in the reason. For each idea, give 2-4 search terms
            (synonyms or the words researchers would use, like "physical activity"
            instead of "working out").

            Answer with JSON only, like this:
            {"concept_groups": [["term", "term"], ["term", "term"]],
             "exclude": [],
             "peer_reviewed_only": true}

            - Each inner list is one idea. Its terms get OR-ed together.
            - The ideas get AND-ed together, so only include ideas that really matter.
            - Don't put AND/OR/NOT, quotes or brackets inside a term.
            - Leave "exclude" empty unless a word has a misleading meaning.
            - peer_reviewed_only should almost always be true.""";

    private final GroqClient groq;
    private final String model;

    public QueryPlanner(GroqClient groq) {
        this(groq, DEFAULT_MODEL);
    }

    public QueryPlanner(GroqClient groq, String model) {
        this.groq = groq;
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model;
    }

    public QueryPlan plan(String thesis, String reason) {
        if (!groq.hasKey()) {
            return checkPlan(fakeAi(reason), "mock (set GROQ_API_KEY to use the AI)");
        }
        Map<String, Object> answer = groq.askForJson(model, INSTRUCTIONS,
                "Thesis: " + thesis + "\nReason: " + reason, "low");
        return checkPlan(answer, model);
    }

    // Validate the AI's answer.
    static QueryPlan checkPlan(Map<String, Object> answer, String modelUsed) {
        if (!(answer.get("concept_groups") instanceof List)) {
            throw new AiException("The AI's answer was missing concept_groups");
        }

        List<List<String>> groups = new ArrayList<>();
        for (Object group : Json.asList(answer.get("concept_groups"))) {
            if (groups.size() == 5) {
                break;
            }
            List<String> terms = new ArrayList<>();
            for (Object term : Json.asList(group)) {
                String cleaned = cleanTerm(Json.asString(term));
                if (!cleaned.isEmpty() && terms.size() < 5) {
                    terms.add(cleaned);
                }
            }
            if (!terms.isEmpty()) {
                groups.add(terms);
            }
        }
        if (groups.isEmpty()) {
            throw new AiException("The AI didn't give us any usable search terms");
        }

        List<String> exclude = new ArrayList<>();
        for (Object term : Json.asList(answer.get("exclude"))) {
            String cleaned = cleanTerm(Json.asString(term));
            if (!cleaned.isEmpty() && exclude.size() < 3) {
                exclude.add(cleaned);
            }
        }

        boolean peerReviewed = !Boolean.FALSE.equals(answer.get("peer_reviewed_only"));
        return new QueryPlan(groups, exclude, peerReviewed, modelUsed);
    }

    // Strip search syntax.
    static String cleanTerm(String term) {
        String cleaned = term.replaceAll("[\"()\\[\\]{}]", " ")
                .replaceAll("\\b(AND|OR|NOT)\\b", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return cleaned.length() > 60 ? cleaned.substring(0, 60).trim() : cleaned;
    }

    private static final Set<String> SKIP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "of", "to", "in", "on", "for", "is", "are", "be",
            "that", "this", "with", "by", "it", "as", "can", "people", "more", "their", "they");

    // Mock: keywords from the reason.
    private static Map<String, Object> fakeAi(String reason) {
        List<Object> groups = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        Matcher words = Pattern.compile("[a-zA-Z][a-zA-Z-]+").matcher(reason.toLowerCase(Locale.ROOT));
        while (words.find() && groups.size() < 4) {
            String word = words.group();
            if (!SKIP_WORDS.contains(word) && !seen.contains(word)) {
                seen.add(word);
                groups.add(List.of(word));
            }
        }
        if (groups.isEmpty()) {
            groups.add(List.of(reason));
        }
        return Map.of("concept_groups", groups, "exclude", List.of(), "peer_reviewed_only", true);
    }
}
