package com.abe.e_likizo.administrative.administrative;

import java.time.LocalDateTime;

public record AdministrativeResponse(
        Long id,
        String name,
        String code,
        String description,
        boolean active,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
