
package com.abe.e_likizo.leave.entity;

import com.abe.e_likizo.leave.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_workflow_history")
@Getter
@Setter
public class LeaveWorkflowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_application_id", nullable = false)
    private LeaveApplication leaveApplication;

    @Column(name = "actor_email", nullable = false, length = 150)
    private String actorEmail;

    @Column(nullable = false, length = 40)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private LeaveStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private LeaveStatus newStatus;

    @Column(nullable = false, length = 1000)
    private String comment;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt = LocalDateTime.now();
}
