package com.evidence_engine.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JdbcTest
@Import(SearchRequestRepository.class)
class SearchRequestRepositoryTest {

    @Autowired
    private SearchRequestRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void addInsertsOneRow() {
        UUID id = UUID.randomUUID();

        repository.add(new SearchRequest(id, "my thesis", "my reason",
                SearchRequest.Status.PENDING, Instant.now()));

        Map<String, Object> row = jdbc
                .sql("SELECT thesis, reason, status FROM search_requests WHERE id = :id")
                .param("id", id)
                .query()
                .singleRow();

        assertEquals("my thesis", row.get("THESIS"));
        assertEquals("my reason", row.get("REASON"));
        assertEquals("PENDING", row.get("STATUS"));
    }
}
