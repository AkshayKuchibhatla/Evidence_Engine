package com.evidence_engine.backend;

import com.evidence_engine.ai.Source;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SearchRequestRepository {

    private final JdbcClient jdbc;

    public SearchRequestRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    //adds one row to the search_requests table
    public void add(SearchRequest request) {
        jdbc.sql("""
                INSERT INTO search_requests
                    (id, thesis, reason, status, created_at, search_query, gmu_search_url, error_message)
                VALUES (:id, :thesis, :reason, :status, :createdAt, :query, :gmuSearchUrl, :error)
                """)
                .param("id", request.id())
                .param("thesis", request.thesis())
                .param("reason", request.reason())
                .param("status", request.status().name())
                .param("createdAt", request.createdAt().atOffset(ZoneOffset.UTC))
                .param("query", cut(request.query(), 5000), Types.VARCHAR)
                .param("gmuSearchUrl", cut(request.gmuSearchUrl(), 10000), Types.VARCHAR)
                .param("error", cut(request.error(), 2000), Types.VARCHAR)
                .update();
        insertSources(request);
    }

    // Saves the new status, query, error and sources of a request that was added earlier
    public void update(SearchRequest request) {
        jdbc.sql("""
                UPDATE search_requests
                SET status = :status, search_query = :query,
                    gmu_search_url = :gmuSearchUrl, error_message = :error
                WHERE id = :id
                """)
                .param("id", request.id())
                .param("status", request.status().name())
                .param("query", cut(request.query(), 5000), Types.VARCHAR)
                .param("gmuSearchUrl", cut(request.gmuSearchUrl(), 10000), Types.VARCHAR)
                .param("error", cut(request.error(), 2000), Types.VARCHAR)
                .update();

        // Replace the sources so saving the same request twice never duplicates them.
        jdbc.sql("DELETE FROM search_sources WHERE request_id = :id")
                .param("id", request.id())
                .update();
        insertSources(request);
    }

    // Loads one request with its sources or empty if not found
    public Optional<SearchRequest> findById(UUID id) {
        return jdbc.sql("""
                SELECT id, thesis, reason, status, created_at, search_query, gmu_search_url, error_message
                FROM search_requests
                WHERE id = :id
                """)
                .param("id", id)
                .query((rs, rowNum) -> new SearchRequest(
                        rs.getObject("id", UUID.class),
                        rs.getString("thesis"),
                        rs.getString("reason"),
                        SearchRequest.Status.valueOf(rs.getString("status")),
                        rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                        rs.getString("search_query"),
                        rs.getString("gmu_search_url"),
                        rs.getString("error_message"),
                        null))
                .optional()
                .map(request -> request.withSources(findSources(id)));
    }

    private List<Source> findSources(UUID requestId) {
        return jdbc.sql("""
                SELECT title, authors, pub_year, journal, abstract_text, doi, link_url
                FROM search_sources
                WHERE request_id = :id
                ORDER BY sort_order
                """)
                .param("id", requestId)
                .query((rs, rowNum) -> new Source(
                        rs.getString("title"),
                        rs.getString("authors"),
                        rs.getString("pub_year"),
                        rs.getString("journal"),
                        rs.getString("abstract_text"),
                        rs.getString("doi"),
                        rs.getString("link_url")))
                .list();
    }

    private void insertSources(SearchRequest request) {
        if (request.sources() == null) {
            return;
        }
        int order = 0;
        for (Source source : request.sources()) {
            jdbc.sql("""
                    INSERT INTO search_sources
                        (request_id, sort_order, title, authors, pub_year, journal, abstract_text, doi, link_url)
                    VALUES (:requestId, :sortOrder, :title, :authors, :pubYear, :journal,
                            :abstractText, :doi, :link)
                    """)
                    .param("requestId", request.id())
                    .param("sortOrder", order++)
                    .param("title", cut(source.title(), 2000), Types.VARCHAR)
                    .param("authors", cut(source.authors(), 4000), Types.VARCHAR)
                    .param("pubYear", cut(source.year(), 20), Types.VARCHAR)
                    .param("journal", cut(source.journal(), 1000), Types.VARCHAR)
                    .param("abstractText", cut(source.abstractText(), 100000), Types.VARCHAR)
                    .param("doi", cut(source.doi(), 500), Types.VARCHAR)
                    .param("link", cut(source.link(), 4000), Types.VARCHAR)
                    .update();
        }
    }
    
    private static String cut(String text, int max) {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }
}
