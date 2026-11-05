package com.evidence_engine.backend;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchRequestService service;

    public SearchController(SearchRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody SearchRequestDto dto) {
        SearchRequest created = service.create(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("id", created.id(), "status", created.status()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SearchRequest> get(@PathVariable UUID id) {
        return service.find(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
