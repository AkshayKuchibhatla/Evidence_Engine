package com.evidence_engine.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SearchRequestService {

    private static final Logger log = LoggerFactory.getLogger(SearchRequestService.class);

    // Placeholder store. Swap for a JPA repository when you add a database.
    private final Map<UUID, SearchRequest> store = new ConcurrentHashMap<>();

    public SearchRequest create(SearchRequestDto dto) {
        SearchRequest request = new SearchRequest(
                UUID.randomUUID(),
                dto.thesis().trim(),
                dto.reason().trim(),
                SearchRequest.Status.PENDING,
                Instant.now()
        );

        store.put(request.id(), request);

        log.info("Search request created id={} status={} thesisLength={} reasonLength={}",
                request.id(), request.status(),
                request.thesis().length(), request.reason().length());
        log.debug("Search request id={} thesis=\"{}\"", request.id(), request.thesis());

        // TODO: hand off to the AI agent
        return request;
    }

    public Optional<SearchRequest> find(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
