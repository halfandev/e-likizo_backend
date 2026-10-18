
package com.abe.e_likizo.employee.service;

import com.abe.e_likizo.administrative.entity.Administrative;
import com.abe.e_likizo.administrative.entity.Region;
import com.abe.e_likizo.administrative.entity.District;
import com.abe.e_likizo.administrative.entity.Station;

import com.abe.e_likizo.administrative.repository.AdministrativeRepository;
import com.abe.e_likizo.administrative.repository.RegionRepository;
import com.abe.e_likizo.administrative.repository.DistrictRepository;
import com.abe.e_likizo.administrative.repository.StationRepository;

import com.abe.e_likizo.employee.dto.EmployeeProfileResponse;
import com.abe.e_likizo.employee.dto.EmployeeRegistrationRequest;
import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final AdministrativeRepository administrativeRepository;
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final StationRepository stationRepository;

    // =========================================
    // 1. MFANYAKAZI KUONA PROFILE YAKE MWENYEWE
    // =========================================
    @Transactional(readOnly = true)
    public EmployeeProfileResponse getMyProfile() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                    "Lazima uingie kwenye mfumo kwanza."
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException(
                        "Taarifa za mfanyakazi hazijapatikana kwa akaunti hii."
                ));

        return toProfileResponse(employee);
    }

    // =========================================
    // 2. ADMIN/HR KUSAJILI PROFILE YA MFANYAKAZI
    // =========================================
    public EmployeeProfileResponse registerEmployee(
            EmployeeRegistrationRequest request) {

        requireAdminOrHr();

        String checknumber = request.checknumber().trim();
        String forcenumber = request.forcenumber().trim();
        String email = normalizeEmail(request.email());

        // Hakikisha Check Number haijatumika
        if (employeeRepository.existsByChecknumber(checknumber)) {
            throw new IllegalArgumentException(
                    "Check Number hii tayari imesajiliwa."
            );
        }

        // Hakikisha Force Number haijatumika
        if (employeeRepository.existsByForcenumber(forcenumber)) {
            throw new IllegalArgumentException(
                    "Force Number hii tayari imesajiliwa."
            );
        }

        // Hakikisha email haijatumika
        if (employeeRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email hii tayari imesajiliwa kwenye profile nyingine."
            );
        }

        Employee employee = new Employee();

        employee.setChecknumber(checknumber);
        employee.setForcenumber(forcenumber);
        employee.setEmail(email);

        employee.setFirstname(request.firstname().trim());

        employee.setMiddlename(
                request.middlename() == null
                        || request.middlename().isBlank()
                        ? null
                        : request.middlename().trim()
        );

        employee.setLastname(request.lastname().trim());

        employee.setGender(
                request.gender().trim().toUpperCase(Locale.ROOT)
        );

        employee.setRank(request.rank());

        // Profile inaweza kusajiliwa kabla akaunti ya User haijatengenezwa
        employee.setUser(null);

        // Weka Administrative, Region, District na Station
        assignWorkLocations(employee, request);

        Employee savedEmployee = employeeRepository.save(employee);

        return toProfileResponse(savedEmployee);
    }

    // =========================================
    // 3. ADMIN/HR KUONA WAFANYAKAZI WOTE
    // =========================================
    @Transactional(readOnly = true)
    public List<EmployeeProfileResponse> getAllEmployees() {

        requireAdminOrHr();

        return employeeRepository.findAll()
                .stream()
                .map(this::toProfileResponse)
                .toList();
    }

    // =========================================
    // 4. ADMIN/HR KUONA MFANYAKAZI MMOJA
    // =========================================
    @Transactional(readOnly = true)
    public EmployeeProfileResponse getEmployeeById(Long id) {

        requireAdminOrHr();

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Mfanyakazi mwenye ID " + id + " hajapatikana."
                ));

        return toProfileResponse(employee);
    }

    // =========================================
    // 5. ADMIN/HR KUBADILISHA TAARIFA ZA MFANYAKAZI
    // =========================================
    public EmployeeProfileResponse updateEmployee(
            Long id,
            EmployeeRegistrationRequest request) {

        requireAdminOrHr();

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Mfanyakazi mwenye ID " + id + " hajapatikana."
                ));

        String checknumber = request.checknumber().trim();
        String forcenumber = request.forcenumber().trim();
        String email = normalizeEmail(request.email());

        // Angalia Check Number
        employeeRepository.findByChecknumber(checknumber)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Check Number hii tayari imesajiliwa."
                    );
                });

        // Angalia Force Number
        employeeRepository.findByForcenumber(forcenumber)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Force Number hii tayari imesajiliwa."
                    );
                });

        // Angalia email
        employeeRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Email hii tayari imesajiliwa."
                    );
                });

        employee.setChecknumber(checknumber);
        employee.setForcenumber(forcenumber);
        employee.setEmail(email);

        employee.setFirstname(request.firstname().trim());

        employee.setMiddlename(
                request.middlename() == null
                        || request.middlename().isBlank()
                        ? null
                        : request.middlename().trim()
        );

        employee.setLastname(request.lastname().trim());

        employee.setGender(
                request.gender().trim().toUpperCase(Locale.ROOT)
        );

        employee.setRank(request.rank());

        // Hakiki na badilisha maeneo ya kazi
        assignWorkLocations(employee, request);

        Employee updatedEmployee = employeeRepository.save(employee);

        return toProfileResponse(updatedEmployee);
    }

    // =========================================
    // 6. ADMIN/HR KUFUTA PROFILE YA MFANYAKAZI
    // =========================================
    public void deleteEmployee(Long id) {

        requireAdminOrHr();

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Mfanyakazi mwenye ID " + id + " hajapatikana."
                ));

        employeeRepository.delete(employee);
    }

    // =========================================
    // 7. KUKAGUA NA KUWEKA MAENEO YA KAZI
    // =========================================
    private void assignWorkLocations(
            Employee employee,
            EmployeeRegistrationRequest request) {

        // Administrative ni lazima
        Administrative administrative =
                administrativeRepository.findById(
                        request.administrativeId()
                ).orElseThrow(() -> new IllegalArgumentException(
                        "Administrative haijapatikana."
                ));

        if (!administrative.isActive()) {
            throw new IllegalArgumentException(
                    "Administrative hii haifanyi kazi."
            );
        }

        Region region = null;
        District district = null;
        Station station = null;

        // Region ni hiari
        if (request.regionId() != null) {

            region = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Region haijapatikana."
                    ));

            if (!region.isActive()) {
                throw new IllegalArgumentException(
                        "Region hii haifanyi kazi."
                );
            }

            if (!Objects.equals(
                    region.getAdministrative().getId(),
                    administrative.getId())) {

                throw new IllegalArgumentException(
                        "Region haipo chini ya Administrative iliyochaguliwa."
                );
            }
        }

        // District ni hiari lakini inahitaji Region
        if (request.districtId() != null) {

            if (region == null) {
                throw new IllegalArgumentException(
                        "Chagua Region kabla ya District."
                );
            }

            district = districtRepository.findById(request.districtId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "District haijapatikana."
                    ));

            if (!district.isActive()) {
                throw new IllegalArgumentException(
                        "District hii haifanyi kazi."
                );
            }

            if (!Objects.equals(
                    district.getRegion().getId(),
                    region.getId())) {

                throw new IllegalArgumentException(
                        "District haipo chini ya Region iliyochaguliwa."
                );
            }
        }

        // Station ni hiari lakini inahitaji District
        if (request.stationId() != null) {

            if (district == null) {
                throw new IllegalArgumentException(
                        "Chagua District kabla ya Station."
                );
            }

            station = stationRepository.findById(request.stationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Station haijapatikana."
                    ));

            if (!station.isActive()) {
                throw new IllegalArgumentException(
                        "Station hii haifanyi kazi."
                );
            }

            if (!Objects.equals(
                    station.getDistrict().getId(),
                    district.getId())) {

                throw new IllegalArgumentException(
                        "Station haipo chini ya District iliyochaguliwa."
                );
            }
        }

        employee.setAdministrative(administrative);
        employee.setRegion(region);
        employee.setDistrict(district);
        employee.setStation(station);
    }

    // =========================================
    // 8. KUKAGUA RUHUSA ZA ADMIN NA HR
    // =========================================
    private void requireAdminOrHr() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                    "Lazima uingie kwenye mfumo kwanza."
            );
        }

        boolean allowed = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(role ->
                        role.equals("ROLE_ADMIN")
                                || role.equals("ROLE_HR")
                                || role.equals("ADMIN")
                                || role.equals("HR")
                );

        if (!allowed) {
            throw new AccessDeniedException(
                    "Admin au HR pekee ndiye anayeruhusiwa "
                            + "kutekeleza operesheni hii."
            );
        }
    }

    // =========================================
    // 9. KUBADILISHA EMAIL KUWA HERUFI NDOGO
    // =========================================
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // =========================================
    // 10. KUBADILISHA EMPLOYEE KUWA DTO
    // =========================================
    private EmployeeProfileResponse toProfileResponse(
            Employee employee) {

        return new EmployeeProfileResponse(
                employee.getId(),
                employee.getChecknumber(),
                employee.getForcenumber(),
                employee.getFirstname(),
                employee.getMiddlename(),
                employee.getLastname(),
                employee.getEmail(),
                employee.getGender(),
                employee.getRank(),
                employee.getAdministrative().getName(),
                employee.getRegion() != null
                        ? employee.getRegion().getName()
                        : null,
                employee.getDistrict() != null
                        ? employee.getDistrict().getName()
                        : null,
                employee.getStation() != null
                        ? employee.getStation().getName()
                        : null
        );
    }
}
