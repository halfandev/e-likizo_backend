
package com.abe.e_likizo.administrative.dto;

import java.time.LocalDateTime;

public record DistrictResponse(
        Long id,
        String name,
        String code,
        String description,
        boolean active,
        Long regionId,
        String regionName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
