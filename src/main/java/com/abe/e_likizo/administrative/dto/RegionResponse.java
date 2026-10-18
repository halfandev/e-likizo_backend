package com.abe.e_likizo.administrative.dto;

import java.time.LocalDateTime;

public record RegionResponse(
        Long id,
        String name,
        String code,
        String description,
        boolean active,
        Long administrativeId,
        String administrativeName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
