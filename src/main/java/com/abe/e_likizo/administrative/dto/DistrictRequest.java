
package com.abe.e_likizo.administrative.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DistrictRequest(

        @NotBlank(message = "Jina la District linahitajika")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Code ya District inahitajika")
        @Size(max = 30)
        String code,

        @Size(max = 255)
        String description,

        Boolean active,

        @NotNull(message = "Chagua Region")
        @Positive(message = "Region ID lazima iwe namba chanya")
        Long regionId

) {
}
