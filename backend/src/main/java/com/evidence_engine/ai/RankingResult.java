package com.evidence_engine.ai;

import java.util.List;

/** Final answer: up to 5 sources (best first), or enoughEvidence=false. */
public record RankingResult(boolean enoughEvidence, List<RankedSource> sources, String message,
                            String model) {
}
