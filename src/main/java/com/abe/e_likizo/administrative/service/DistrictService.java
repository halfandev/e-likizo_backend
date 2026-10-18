
package com.abe.e_likizo.administrative.service;

import com.abe.e_likizo.administrative.dto.DistrictRequest;
import com.abe.e_likizo.administrative.dto.DistrictResponse;
import com.abe.e_likizo.administrative.entity.District;
import com.abe.e_likizo.administrative.entity.Region;
import com.abe.e_likizo.administrative.repository.DistrictRepository;
import com.abe.e_likizo.administrative.repository.RegionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;

    // CREATE
    public DistrictResponse create(DistrictRequest request) {

        String code = normalizeCode(request.code());

        if (districtRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii ya District tayari imetumika"
            );
        }

        Region region = findRegion(request.regionId());

        District district = new District();

        district.setName(request.name().trim());
        district.setCode(code);
        district.setDescription(
                normalizeDescription(request.description())
        );
        district.setActive(
                request.active() == null || request.active()
        );
        district.setRegion(region);

        District saved = districtRepository.save(district);

        return toResponse(saved);
    }

    // READ ALL
    @Transactional(readOnly = true)
    public List<DistrictResponse> getAll() {

        return districtRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // READ ONE
    @Transactional(readOnly = true)
    public DistrictResponse getById(Long id) {

        return toResponse(findDistrict(id));
    }

    // READ DISTRICTS BY REGION
    @Transactional(readOnly = true)
    public List<DistrictResponse> getByRegionId(Long regionId) {

        // Hakikisha Region ipo
        findRegion(regionId);

        return districtRepository.findByRegionId(regionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // UPDATE
    public DistrictResponse update(
            Long id,
            DistrictRequest request) {

        District district = findDistrict(id);

        String code = normalizeCode(request.code());

        if (districtRepository.existsByCodeAndIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii inatumiwa na District nyingine"
            );
        }

        Region region = findRegion(request.regionId());

        district.setName(request.name().trim());
        district.setCode(code);
        district.setDescription(
                normalizeDescription(request.description())
        );
        district.setRegion(region);

        if (request.active() != null) {
            district.setActive(request.active());
        }

        District updated = districtRepository.save(district);

        return toResponse(updated);
    }

    // DELETE
    public void delete(Long id) {

        District district = findDistrict(id);

        districtRepository.delete(district);
    }

    private District findDistrict(Long id) {

        return districtRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "District haijapatikana kwa ID: " + id
                ));
    }

    private Region findRegion(Long id) {

        return regionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Region haijapatikana kwa ID: " + id
                ));
    }

    private String normalizeCode(String code) {

        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {

        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private DistrictResponse toResponse(District district) {

        return new DistrictResponse(
                district.getId(),
                district.getName(),
                district.getCode(),
                district.getDescription(),
                district.isActive(),
                district.getRegion().getId(),
                district.getRegion().getName(),
                district.getCreatedAt(),
                district.getCreatedBy(),
                district.getUpdatedAt(),
                district.getUpdatedBy()
        );
    }
}
