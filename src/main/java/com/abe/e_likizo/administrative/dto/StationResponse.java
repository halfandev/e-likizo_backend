
package com.abe.e_likizo.administrative.dto;

import java.time.LocalDateTime;

public record StationResponse(
        Long id,
        String name,
        String code,
        String description,
        boolean active,
        Long districtId,
        String districtName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
