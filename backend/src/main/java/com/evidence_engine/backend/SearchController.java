package com.evidence_engine.backend;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchRequestService service;

    public SearchController(SearchRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SearchRequest> submit(@Valid @RequestBody SearchRequestDto dto) throws IOException {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SearchRequest> get(@PathVariable UUID id) {
        return service.find(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
