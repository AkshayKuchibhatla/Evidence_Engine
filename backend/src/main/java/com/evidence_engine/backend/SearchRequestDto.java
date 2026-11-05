package com.evidence_engine.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchRequestDto(
        @NotBlank @Size(max = 2000) String thesis,
        @NotBlank @Size(max = 5000) String reason
){}
