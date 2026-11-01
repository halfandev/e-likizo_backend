package com.abe.e_likizo.service;

import com.abe.e_likizo.dto.AdminCreateUserRequest;
import com.abe.e_likizo.dto.OnlineUserResponse;
import com.abe.e_likizo.dto.UserResponse;

import com.abe.e_likizo.entity.User;
import com.abe.e_likizo.entity.Role;

import com.abe.e_likizo.repository.UserRepository;

import com.abe.e_likizo.administrative.entity.Administrative;
import com.abe.e_likizo.administrative.entity.Region;
import com.abe.e_likizo.administrative.entity.District;
import com.abe.e_likizo.administrative.entity.Station;

import com.abe.e_likizo.administrative.repository.AdministrativeRepository;
import com.abe.e_likizo.administrative.repository.RegionRepository;
import com.abe.e_likizo.administrative.repository.DistrictRepository;
import com.abe.e_likizo.administrative.repository.StationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordGeneratorService passwordGeneratorService;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final AdministrativeRepository administrativeRepository;

    private final RegionRepository regionRepository;

    private final DistrictRepository districtRepository;

    private final StationRepository stationRepository;


    // =====================================================
    // GET ALL USERS
    // =====================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =====================================================
    // MAP USER TO RESPONSE
    // =====================================================

    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .mustChangePassword(user.isMustChangePassword())
                .lastLogin(user.getLastLogin())
                .lastLogout(user.getLastLogout())
                .createdAt(user.getCreatedAt())

                .administrativeId(
                        user.getAdministrative() != null
                                ? user.getAdministrative().getId() : null
                )
                .administrativeName(
                        user.getAdministrative() != null
                                ? user.getAdministrative().getName() : null
                )

                .regionId(
                        user.getRegion() != null
                                ? user.getRegion().getId() : null
                )
                .regionName(
                        user.getRegion() != null
                                ? user.getRegion().getName() : null
                )

                .districtId(
                        user.getDistrict() != null
                                ? user.getDistrict().getId() : null
                )
                .districtName(
                        user.getDistrict() != null
                                ? user.getDistrict().getName() : null
                )

                .stationId(
                        user.getStation() != null
                                ? user.getStation().getId() : null
                )
                .stationName(
                        user.getStation() != null
                                ? user.getStation().getName() : null
                )

                .build();
    }


    // =====================================================
    // DISABLE USER
    // =====================================================

    @Transactional
    public UserResponse disableUser(Long id) {
        return updateUserStatus(id, false);
    }


    // =====================================================
    // ENABLE USER
    // =====================================================

    @Transactional
    public UserResponse enableUser(Long id) {
        return updateUserStatus(id, true);
    }


    // =====================================================
    // UPDATE USER STATUS
    // =====================================================

    private UserResponse updateUserStatus(Long id, boolean enabled) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with ID: " + id
                ));

        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    // =====================================================
    // RECORD USER ACTIVITY
    // =====================================================

    @Transactional
    public void recordActivity(User user) {

        user.setLastActivity(LocalDateTime.now());

        userRepository.save(user);
    }


    // =====================================================
    // GET ONLINE USERS
    // =====================================================

    @Transactional(readOnly = true)
    public List<OnlineUserResponse> getOnlineUsers() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime cutoff = now.minusMinutes(5);

        return userRepository.findAll()
                .stream()

                .filter(User::isEnabled)

                .filter(user -> user.getLastLogin() != null)

                .filter(user -> user.getLastActivity() != null)

                .filter(user -> user.getLastActivity().isAfter(cutoff))

                .filter(user ->
                        user.getLastLogout() == null
                                || user.getLastLogout().isBefore(user.getLastLogin())
                )

                .map(user -> OnlineUserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .lastLogin(user.getLastLogin())
                        .lastActivity(user.getLastActivity())

                        .onlineDurationMinutes(
                                Math.max(
                                        0,
                                        Duration.between(
                                                user.getLastLogin(),
                                                now
                                        ).toMinutes()
                                )
                        )

                        .build()
                )

                .toList();
    }


    // =====================================================
    // CREATE USER BY ADMIN
    // =====================================================

    @Transactional
    public UserResponse createUserByAdmin(AdminCreateUserRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        Role role = request.getRole();

        if (role == null || role == Role.ADMIN) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role for this operation"
            );
        }

        // Get leadership location according to role
        LocationAssignment location = assignLocation(request, role);

        String temporaryPassword =
                passwordGeneratorService.generatePassword(12);

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()

                .fullName(request.getFullName().trim())

                .email(email)

                .password(passwordEncoder.encode(temporaryPassword))

                .role(role)

                .administrative(location.administrative())
                .region(location.region())
                .district(location.district())
                .station(location.station())

                .enabled(true)

                .mustChangePassword(true)

                .createdAt(now)

                .updatedAt(now)

                .build();

        User savedUser = userRepository.save(user);

        emailService.sendLoginCredentials(
                savedUser.getEmail(),
                savedUser.getFullName(),
                temporaryPassword
        );

        return mapToResponse(savedUser);
    }


    // =====================================================
    // ASSIGN LOCATION ACCORDING TO ROLE
    // =====================================================

    private LocationAssignment assignLocation(
            AdminCreateUserRequest request,
            Role role
    ) {

        return switch (role) {

            case OCS -> {

                if (request.getStationId() == null) {
                    throw badRequest("Station is required for OCS");
                }

                Station station = stationRepository
                        .findById(request.getStationId())
                        .orElseThrow(() -> notFound("Station not found"));

                if (!station.isActive()
                        || !station.getDistrict().isActive()
                        || !station.getDistrict().getRegion().isActive()
                        || !station.getDistrict().getRegion()
                        .getAdministrative().isActive()) {

                    throw badRequest("Selected station hierarchy is inactive");
                }

                District district = station.getDistrict();

                Region region = district.getRegion();

                Administrative administrative =
                        region.getAdministrative();

                yield new LocationAssignment(
                        administrative,
                        region,
                        district,
                        station
                );
            }


            case OCD -> {

                if (request.getDistrictId() == null) {
                    throw badRequest("District is required for OCD");
                }

                District district = districtRepository
                        .findById(request.getDistrictId())
                        .orElseThrow(() -> notFound("District not found"));

                if (!district.isActive()
                        || !district.getRegion().isActive()
                        || !district.getRegion()
                        .getAdministrative().isActive()) {

                    throw badRequest("Selected district hierarchy is inactive");
                }

                Region region = district.getRegion();

                Administrative administrative =
                        region.getAdministrative();

                yield new LocationAssignment(
                        administrative,
                        region,
                        district,
                        null
                );
            }


            case RPC -> {

                if (request.getRegionId() == null) {
                    throw badRequest("Region is required for RPC");
                }

                Region region = regionRepository
                        .findById(request.getRegionId())
                        .orElseThrow(() -> notFound("Region not found"));

                if (!region.isActive()
                        || !region.getAdministrative().isActive()) {

                    throw badRequest("Selected region is inactive");
                }

                Administrative administrative =
                        region.getAdministrative();

                yield new LocationAssignment(
                        administrative,
                        region,
                        null,
                        null
                );
            }


            case CP -> {

                if (request.getAdministrativeId() == null) {
                    throw badRequest(
                            "Administrative is required for CP"
                    );
                }

                Administrative administrative =
                        getActiveAdministrative(
                                request.getAdministrativeId()
                        );

                if (!isZanzibarPoliceForce(administrative)) {

                    throw badRequest(
                            "CP is only allowed under Zanzibar Police Force"
                    );
                }

                yield new LocationAssignment(
                        administrative,
                        null,
                        null,
                        null
                );
            }


            case IGP -> {

                if (request.getAdministrativeId() == null) {
                    throw badRequest(
                            "Administrative is required for IGP"
                    );
                }

                Administrative administrative =
                        getActiveAdministrative(
                                request.getAdministrativeId()
                        );

                yield new LocationAssignment(
                        administrative,
                        null,
                        null,
                        null
                );
            }


            default -> {

                // Other roles do not require leadership locations.
                yield new LocationAssignment(
                        null,
                        null,
                        null,
                        null
                );
            }
        };
    }


    // =====================================================
    // GET ACTIVE ADMINISTRATIVE
    // =====================================================

    private Administrative getActiveAdministrative(Long id) {

        Administrative administrative = administrativeRepository
                .findById(id)
                .orElseThrow(() -> notFound("Administrative not found"));

        if (!administrative.isActive()) {

            throw badRequest("Administrative is inactive");
        }

        return administrative;
    }


    // =====================================================
    // CHECK ZANZIBAR POLICE FORCE
    // =====================================================

    private boolean isZanzibarPoliceForce(
            Administrative administrative
    ) {

        String name = administrative.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        return name.equals("zanzibar police force");
    }


    // =====================================================
    // EXCEPTION HELPERS
    // =====================================================

    private ResponseStatusException badRequest(String message) {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }


    private ResponseStatusException notFound(String message) {

        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                message
        );
    }


    // =====================================================
    // LOCATION ASSIGNMENT RECORD
    // =====================================================

    private record LocationAssignment(
            Administrative administrative,
            Region region,
            District district,
            Station station
    ) {
    }

}