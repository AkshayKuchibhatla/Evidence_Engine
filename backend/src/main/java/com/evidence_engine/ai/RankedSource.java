package com.evidence_engine.backend.ai;

/** A picked source: verified quote, why it fits, score 0-10. */
public record RankedSource(Source source, String quote, String why, int score) {
}
