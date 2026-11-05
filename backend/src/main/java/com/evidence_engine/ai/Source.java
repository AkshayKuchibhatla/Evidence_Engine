package com.evidence_engine.backend.ai;

/** One library result. Needs an abstract to be quoted. */
public record Source(String title, String authors, String year, String journal,
                     String abstractText, String doi, String link) {
}
