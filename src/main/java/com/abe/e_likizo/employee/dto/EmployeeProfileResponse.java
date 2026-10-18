
package com.abe.e_likizo.employee.dto;

import com.abe.e_likizo.employee.enums.Rank;

public record EmployeeProfileResponse(
        Long id,
        String checknumber,
        String forcenumber,
        String firstname,
        String middlename,
        String lastname,
        String email,
        String gender,
        Rank rank,
        String administrative,
        String region,
        String district,
        String station
) {}
