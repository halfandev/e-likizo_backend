package com.abe.e_likizo.leave.entity;

import com.abe.e_likizo.administrative.audit.AuditableEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "leave_documents")
@Getter
@Setter
public class LeaveDocument extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ombi la likizo ambalo nyaraka hii inahusika nalo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_application_id", nullable = false)
    private LeaveApplication leaveApplication;

    // Jina la faili lililopakiwa na mfanyakazi
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    // Jina la kipekee la faili lililohifadhiwa kwenye server
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    // Aina ya faili, kwa mfano application/pdf
    @Column(name = "content_type", length = 100)
    private String contentType;

    // Mahali faili lilipohifadhiwa
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // Ukubwa wa faili kwa bytes
    @Column(name = "file_size")
    private Long fileSize;

    // Maelezo mafupi kuhusu nyaraka
    @Column(length = 500)
    private String description;
}
