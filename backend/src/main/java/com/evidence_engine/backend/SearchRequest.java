package com.evidence_engine.backend;

import com.evidence_engine.ai.QueryPlan;
import java.time.Instant;
import java.util.UUID;

public record SearchRequest(
        UUID id,
        String thesis,
        String reason,
        Status status,
        Instant createdAt,
        String query,          // boolean search string from the AI
        String gmuSearchUrl,   // same search as a GMU library link
        String error,          // why it failed, if it did
        String response		   // the response JSON
) {
    public enum Status { PENDING, RUNNING, COMPLETED, FAILED }
    
    public SearchRequest withPlan(QueryPlan plan) {
        return new SearchRequest(id, thesis, reason, Status.COMPLETED, createdAt,
                plan.booleanQuery(), plan.gmuSearchUrl(), null, null);
    }

    public SearchRequest failed(String message) {
        return new SearchRequest(id, thesis, reason, Status.FAILED, createdAt,
                null, null, message, null);
    }
    
    public SearchRequest withResponse(String response) {
        return new SearchRequest(id, thesis, reason, status, createdAt,
                query, gmuSearchUrl, error, response);
    }
}
