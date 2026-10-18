package com.abe.e_likizo.administrative.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdministrativeRequest(

        @NotBlank(message = "Jina la Command/Force linahitajika")
        @Size(max = 100, message = "Jina lisizidi herufi 100")
        String name,

        @NotBlank(message = "Code inahitajika")
        @Size(max = 30, message = "Code isizidi herufi 30")
        String code,

        @Size(max = 255, message = "Maelezo yasizidi herufi 255")
        String description,

        Boolean active

) {
}
