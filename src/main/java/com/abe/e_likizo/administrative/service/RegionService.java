package com.abe.e_likizo.administrative.service;

import com.abe.e_likizo.administrative.dto.RegionRequest;
import com.abe.e_likizo.administrative.dto.RegionResponse;
import com.abe.e_likizo.administrative.entity.Administrative;
import com.abe.e_likizo.administrative.entity.Region;
import com.abe.e_likizo.administrative.repository.AdministrativeRepository;
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
public class RegionService {

    private final RegionRepository regionRepository;
    private final AdministrativeRepository administrativeRepository;

    // CREATE
    public RegionResponse create(RegionRequest request) {

        String code = normalizeCode(request.code());

        if (regionRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii ya Region tayari imetumika"
            );
        }

        Administrative administrative =
                findAdministrative(request.administrativeId());

        Region region = new Region();

        region.setName(request.name().trim());
        region.setCode(code);
        region.setDescription(
                normalizeDescription(request.description())
        );
        region.setActive(
                request.active() == null || request.active()
        );
        region.setAdministrative(administrative);

        Region saved = regionRepository.save(region);

        return toResponse(saved);
    }

    // READ ALL
    @Transactional(readOnly = true)
    public List<RegionResponse> getAll() {

        return regionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // READ ONE
    @Transactional(readOnly = true)
    public RegionResponse getById(Long id) {

        return toResponse(findRegion(id));
    }

    // READ REGIONS BY ADMINISTRATIVE
    @Transactional(readOnly = true)
    public List<RegionResponse> getByAdministrativeId(
            Long administrativeId) {

        // Thibitisha Administrative ipo
        findAdministrative(administrativeId);

        return regionRepository
                .findByAdministrativeId(administrativeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // UPDATE
    public RegionResponse update(
            Long id,
            RegionRequest request) {

        Region region = findRegion(id);

        String code = normalizeCode(request.code());

        if (regionRepository.existsByCodeAndIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii ya Region inatumiwa na rekodi nyingine"
            );
        }

        Administrative administrative =
                findAdministrative(request.administrativeId());

        region.setName(request.name().trim());
        region.setCode(code);
        region.setDescription(
                normalizeDescription(request.description())
        );
        region.setAdministrative(administrative);

        if (request.active() != null) {
            region.setActive(request.active());
        }

        Region updated = regionRepository.save(region);

        return toResponse(updated);
    }

    // DELETE
    public void delete(Long id) {

        Region region = findRegion(id);

        regionRepository.delete(region);
    }

    private Region findRegion(Long id) {

        return regionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Region haijapatikana kwa ID: " + id
                ));
    }

    private Administrative findAdministrative(Long id) {

        return administrativeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Administrative haijapatikana kwa ID: " + id
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

    private RegionResponse toResponse(Region region) {

        return new RegionResponse(
                region.getId(),
                region.getName(),
                region.getCode(),
                region.getDescription(),
                region.isActive(),
                region.getAdministrative().getId(),
                region.getAdministrative().getName(),
                region.getCreatedAt(),
                region.getCreatedBy(),
                region.getUpdatedAt(),
                region.getUpdatedBy()
        );
    }
}