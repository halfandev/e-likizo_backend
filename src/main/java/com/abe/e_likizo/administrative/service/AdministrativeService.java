package com.abe.e_likizo.administrative.service;

import com.abe.e_likizo.administrative.administrative.AdministrativeResponse;
import com.abe.e_likizo.administrative.dto.AdministrativeRequest;
import com.abe.e_likizo.administrative.entity.Administrative;
import com.abe.e_likizo.administrative.repository.AdministrativeRepository;

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
public class AdministrativeService {

    private final AdministrativeRepository repository;

    public AdministrativeResponse create(
            AdministrativeRequest request) {

        String code = normalizeCode(request.code());

        if (repository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii tayari imetumika"
            );
        }

        Administrative administrative = new Administrative();

        administrative.setName(request.name().trim());
        administrative.setCode(code);
        administrative.setDescription(
                normalizeDescription(request.description())
        );
        administrative.setActive(
                request.active() == null || request.active()
        );

        Administrative saved = repository.save(administrative);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AdministrativeResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdministrativeResponse getById(Long id) {
        return toResponse(findAdministrative(id));
    }

    public AdministrativeResponse update(
            Long id,
            AdministrativeRequest request) {

        Administrative administrative = findAdministrative(id);
        String code = normalizeCode(request.code());

        if (repository.existsByCodeAndIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii tayari inatumiwa na rekodi nyingine"
            );
        }

        administrative.setName(request.name().trim());
        administrative.setCode(code);
        administrative.setDescription(
                normalizeDescription(request.description())
        );

        if (request.active() != null) {
            administrative.setActive(request.active());
        }

        Administrative updated = repository.save(administrative);

        return toResponse(updated);
    }

    public void delete(Long id) {
        Administrative administrative = findAdministrative(id);
        repository.delete(administrative);
    }

    private Administrative findAdministrative(Long id) {
        return repository.findById(id)
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

    private AdministrativeResponse toResponse(
            Administrative administrative) {

        return new AdministrativeResponse(
                administrative.getId(),
                administrative.getName(),
                administrative.getCode(),
                administrative.getDescription(),
                administrative.isActive(),
                administrative.getCreatedAt(),
                administrative.getCreatedBy(),
                administrative.getUpdatedAt(),
                administrative.getUpdatedBy()
        );
    }
}
