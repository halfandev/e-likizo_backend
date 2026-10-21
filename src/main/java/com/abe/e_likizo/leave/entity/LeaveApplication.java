package com.abe.e_likizo.leave.entity;

import com.abe.e_likizo.administrative.audit.AuditableEntity;
import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.leave.enums.LeaveStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leave_applications")
@Getter
@Setter
public class LeaveApplication extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee mmoja anaweza kuwa na maombi mengi
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Kila ombi linakuwa na aina moja ya likizo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "requested_days", nullable = false)
    private Integer requestedDays;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeaveStatus status = LeaveStatus.PENDING;

    // Mwanafamilia anayehusishwa na ombi la likizo.
// Ni optional kwa aina nyingine za likizo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id")
    private FamilyMember familyMember;

    // Nyaraka zinazohusiana na ombi hili la likizo.
    @OneToMany(
            mappedBy = "leaveApplication",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<LeaveDocument> documents = new ArrayList<>();
}