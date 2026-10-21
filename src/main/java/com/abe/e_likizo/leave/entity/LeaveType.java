package com.abe.e_likizo.leave.entity;

import com.abe.e_likizo.administrative.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "leave_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_leave_type_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
public class LeaveType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "default_days", nullable = false)
    private Integer defaultDays;

    @Column(nullable = false)
    private boolean requiresDocument;

    @Column(nullable = false)
    private boolean active = true;
}