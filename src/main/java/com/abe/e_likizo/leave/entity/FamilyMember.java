
package com.abe.e_likizo.leave.entity;

import com.abe.e_likizo.administrative.audit.AuditableEntity;
import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.leave.enums.FamilyRelationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "family_members")
@Getter
@Setter
public class FamilyMember extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mfanyakazi anayemiliki taarifa hizi
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Jina la mwanafamilia
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    // Uhusiano na mfanyakazi
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 20)
    private FamilyRelationship relationship;

    // Tarehe ya kuzaliwa; inaweza kuachwa ikiwa haijulikani
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Taarifa za ziada, ikiwa zinahitajika
    @Column(length = 500)
    private String notes;
}
