package com.evidence_engine.backend;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.time.ZoneOffset;

@Repository
public class SearchRequestRepository {

    private final JdbcClient jdbc;

    public SearchRequestRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    //adds one row to the search_requests table
    public void add(SearchRequest request) {
        jdbc.sql("""
                INSERT INTO search_requests (id, thesis, reason, status, created_at)
                VALUES (:id, :thesis, :reason, :status, :createdAt)
                """)
                .param("id", request.id())
                .param("thesis", request.thesis())
                .param("reason", request.reason())
                .param("status", request.status().name())
                .param("createdAt", request.createdAt().atOffset(ZoneOffset.UTC))
                .update();
    }
}
