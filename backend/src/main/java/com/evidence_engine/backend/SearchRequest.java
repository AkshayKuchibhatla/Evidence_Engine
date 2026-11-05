package com.evidence_engine.backend;

import java.time.Instant;
import java.util.UUID;

public record SearchRequest(
        UUID id,
        String thesis,
        String reason,
        Status status,
        Instant createdAt
) {
    public enum Status { PENDING, RUNNING, COMPLETED, FAILED }
}
