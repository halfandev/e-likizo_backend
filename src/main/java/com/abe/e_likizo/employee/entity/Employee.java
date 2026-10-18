
package com.abe.e_likizo.employee.entity;

import com.abe.e_likizo.administrative.audit.AuditableEntity;
import com.abe.e_likizo.administrative.entity.Administrative;
import com.abe.e_likizo.administrative.entity.Region;
import com.abe.e_likizo.administrative.entity.District;
import com.abe.e_likizo.administrative.entity.Station;
import com.abe.e_likizo.employee.enums.Rank;
import com.abe.e_likizo.entity.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_checknumber",
                        columnNames = "checknumber"
                ),
                @UniqueConstraint(
                        name = "uk_employee_forcenumber",
                        columnNames = "forcenumber"
                ),
                @UniqueConstraint(
                        name = "uk_employee_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_employee_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Setter
public class Employee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee identification numbers
    @Column(name = "checknumber", nullable = false, length = 50)
    private String checknumber;

    @Column(name = "forcenumber", nullable = false, length = 50)
    private String forcenumber;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;
    // Employee personal information
    @Column(name = "firstname", nullable = false, length = 100)
    private String firstname;

    @Column(name = "middlename", length = 100)
    private String middlename;

    @Column(name = "lastname", nullable = false, length = 100)
    private String lastname;



    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    // Login account is optional until the employee registers
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "user_id",
            nullable = true,
            unique = true
    )
    private User user;

    // Required administrative organization
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrative_id", nullable = false)
    private Administrative administrative;

    // Optional workplace hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    // Employee rank
    @Enumerated(EnumType.STRING)
    @Column(name = "rank", nullable = false, length = 20)
    private Rank rank;
}
