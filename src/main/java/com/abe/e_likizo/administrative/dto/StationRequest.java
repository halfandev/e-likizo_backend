
package com.abe.e_likizo.administrative.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StationRequest(

        @NotBlank(message = "Jina la Station linahitajika")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Code ya Station inahitajika")
        @Size(max = 30)
        String code,

        @Size(max = 255)
        String description,

        Boolean active,

        @NotNull(message = "District inahitajika")
        @Positive(message = "District ID lazima iwe kubwa kuliko sifuri")
        Long districtId

) {
}
