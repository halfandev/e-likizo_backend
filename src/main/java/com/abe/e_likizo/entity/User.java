package com.abe.e_likizo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.abe.e_likizo.administrative.entity.Administrative;
import com.abe.e_likizo.administrative.entity.Region;
import com.abe.e_likizo.administrative.entity.District;
import com.abe.e_likizo.administrative.entity.Station;
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrative_id")
    private Administrative administrative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    private boolean enabled;

    private boolean mustChangePassword;

    private LocalDateTime lastLogin;

    private LocalDateTime lastLogout;

    private LocalDateTime lastActivity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
