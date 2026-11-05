package com.evidence_engine.backend.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Step 2: picks the top 5 results with one quote each (reasoning model). Fake quotes are dropped. */
public class SourceRanker {

    public static final String DEFAULT_MODEL = "openai/gpt-oss-120b";
    public static final int TOP_N = 5;

    private static final int MIN_SCORE = 6;
    private static final int MAX_CANDIDATES = 25;
    private static final int MAX_ABSTRACT_LENGTH = 1500;
    private static final int MIN_QUOTE_LENGTH = 20;
    private static final int MAX_QUOTE_LENGTH = 400;

    private static final String INSTRUCTIONS = """
            You check library sources for a college student's essay.
            You will get the student's thesis, ONE reason that supports it, and a numbered list
            of sources (title, year, abstract).

            Pick the sources whose abstract gives evidence for the REASON. Being on the same
            general topic is not enough. Skip sources that are off topic or argue against it.

            For each source you pick:
            - "quote": copy ONE sentence (or part of one sentence) from its abstract, word for word.
              Don't reword it, don't join sentences, don't use "...". Under 50 words.
            - "why": one sentence on how the quote supports the reason.
            - "score": 0-10. 10 = direct evidence for the reason, 6 = clear but indirect support,
              below 6 = weak.

            Pick at most 5, best first. If none of the sources really support the reason,
            return an empty list.

            Answer with JSON only, like this:
            {"picks": [{"id": "S1", "quote": "...", "why": "...", "score": 8}]}""";

    private final GroqClient groq;
    private final String model;

    public SourceRanker(GroqClient groq) {
        this(groq, DEFAULT_MODEL);
    }

    public SourceRanker(GroqClient groq, String model) {
        this.groq = groq;
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model;
    }

    public RankingResult rank(String thesis, String reason, List<Source> results) {
        // Ids S1, S2... for the AI; skip duplicates and no-abstract results.
        Map<String, Source> candidates = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        for (Source source : results) {
            if (candidates.size() == MAX_CANDIDATES) {
                break;
            }
            String text = cleanText(source.abstractText());
            if (text.length() < 50) {
                continue;
            }
            String key = source.doi() != null && !source.doi().isBlank()
                    ? source.doi().toLowerCase(Locale.ROOT)
                    : cleanText(source.title()).toLowerCase(Locale.ROOT);
            if (seen.add(key)) {
                candidates.put("S" + (candidates.size() + 1), source);
            }
        }

        if (candidates.isEmpty()) {
            return notEnough("The library search didn't return any sources with an abstract to check.",
                    groq.hasKey() ? model : "mock");
        }

        List<RankedSource> picks;
        String modelUsed;
        if (groq.hasKey()) {
            Map<String, Object> answer = groq.askForJson(model, INSTRUCTIONS,
                    buildMessage(thesis, reason, candidates), "medium");
            picks = checkPicks(answer, candidates);
            modelUsed = model;
        } else {
            picks = fakeAi(reason, candidates);
            modelUsed = "mock (set GROQ_API_KEY to use the AI)";
        }

        if (picks.isEmpty()) {
            return notEnough("Not enough evidence: none of the " + candidates.size()
                    + " sources clearly support this reason. Try rewording it or making it broader.", modelUsed);
        }
        return new RankingResult(true, picks,
                "Found " + picks.size() + " source" + (picks.size() == 1 ? "" : "s") + " that support this reason.",
                modelUsed);
    }

    private static String buildMessage(String thesis, String reason, Map<String, Source> candidates) {
        StringBuilder message = new StringBuilder()
                .append("Thesis: ").append(thesis).append('\n')
                .append("Reason: ").append(reason).append("\n\nSources:\n");
        candidates.forEach((id, source) -> {
            String text = cleanText(source.abstractText());
            if (text.length() > MAX_ABSTRACT_LENGTH) {
                text = text.substring(0, MAX_ABSTRACT_LENGTH) + " [cut off]";
            }
            message.append("\n[").append(id).append("] ").append(cleanText(source.title()));
            if (source.year() != null && !source.year().isBlank()) {
                message.append(" (").append(source.year()).append(')');
            }
            message.append("\nAbstract: ").append(text).append('\n');
        });
        return message.toString();
    }

    // Keep real sources with real quotes and good scores.
    static List<RankedSource> checkPicks(Map<String, Object> answer, Map<String, Source> candidates) {
        List<RankedSource> picks = new ArrayList<>();
        Set<String> used = new HashSet<>();

        for (Object item : Json.asList(answer.get("picks"))) {
            Map<String, Object> pick = Json.asMap(item);
            String id = Json.asString(pick.get("id")).trim().toUpperCase(Locale.ROOT);
            Source source = candidates.get(id);
            if (source == null || used.contains(id)) {
                continue;
            }

            int score = toScore(pick.get("score"));
            String quote = Json.asString(pick.get("quote")).trim();
            String why = Json.asString(pick.get("why")).trim();
            if (score < MIN_SCORE || !quoteIsReal(quote, source.abstractText())) {
                continue;
            }

            used.add(id);
            picks.add(new RankedSource(source, stripOuterQuotes(quote), why, score));
        }

        // Best first.
        picks.sort(Comparator.comparingInt(RankedSource::score).reversed());
        return picks.size() > TOP_N ? new ArrayList<>(picks.subList(0, TOP_N)) : picks;
    }

    /** Quote appears word for word in the abstract. */
    static boolean quoteIsReal(String quote, String abstractText) {
        String q = normalize(stripOuterQuotes(quote));
        if (q.length() < MIN_QUOTE_LENGTH || q.length() > MAX_QUOTE_LENGTH
                || q.contains("...") || q.contains("…")) {
            return false;
        }
        return normalize(cleanText(abstractText)).contains(q);
    }

    private static int toScore(Object value) {
        if (value instanceof Number n) {
            return (int) Math.round(n.doubleValue());
        }
        try {
            return (int) Math.round(Double.parseDouble(Json.asString(value)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Strip HTML from abstracts.
    static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]+>", " ")
                .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'").replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Unify quotes, dashes, spaces.
    private static String normalize(String text) {
        return text.replaceAll("[‘’‛′]", "'")
                .replaceAll("[“”‟″]", "\"")
                .replaceAll("[‐-―−]", "-")
                .replaceAll("\\s+", " ")
                .trim()
                .replaceAll("[.;,]+$", "");
    }

    private static String stripOuterQuotes(String quote) {
        String q = quote.trim();
        while (q.length() > 1 && "\"'“”‘’".indexOf(q.charAt(0)) >= 0
                && "\"'“”‘’".indexOf(q.charAt(q.length() - 1)) >= 0) {
            q = q.substring(1, q.length() - 1).trim();
        }
        return q;
    }

    private static RankingResult notEnough(String message, String model) {
        return new RankingResult(false, List.of(), message, model);
    }

    // ---------- mock mode (no Groq key) ----------

    private static final Set<String> SKIP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "of", "to", "in", "on", "for", "is", "are", "be", "that",
            "this", "with", "by", "it", "as", "can", "people", "more", "their", "they", "who", "have");

    // Mock: rank by keyword matches.
    private static List<RankedSource> fakeAi(String reason, Map<String, Source> candidates) {
        Set<String> keywords = new HashSet<>();
        Matcher words = Pattern.compile("[a-z][a-z-]{2,}").matcher(reason.toLowerCase(Locale.ROOT));
        while (words.find()) {
            if (!SKIP_WORDS.contains(words.group())) {
                keywords.add(words.group());
            }
        }

        List<RankedSource> picks = new ArrayList<>();
        for (Source source : candidates.values()) {
            String bestSentence = "";
            int bestHits = 0;
            // Split sentences and bullets.
            for (String sentence : cleanText(source.abstractText()).split("(?<=[.!?])\\s+|\\s*•\\s*")) {
                String lower = sentence.toLowerCase(Locale.ROOT);
                int hits = (int) keywords.stream().filter(lower::contains).count();
                if (hits > bestHits && sentence.length() >= MIN_QUOTE_LENGTH
                        && sentence.length() <= MAX_QUOTE_LENGTH) {
                    bestHits = hits;
                    bestSentence = sentence;
                }
            }
            if (bestHits >= 2 || (bestHits == 1 && keywords.size() == 1)) {
                picks.add(new RankedSource(source, bestSentence,
                        "Mock mode: this sentence mentions " + bestHits + " words from your reason.",
                        Math.min(10, 4 + bestHits * 2)));
            }
        }
        picks.sort(Comparator.comparingInt(RankedSource::score).reversed());
        return picks.size() > TOP_N ? new ArrayList<>(picks.subList(0, TOP_N)) : picks;
    }
}
