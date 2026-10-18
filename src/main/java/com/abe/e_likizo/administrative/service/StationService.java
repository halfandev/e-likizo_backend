
package com.abe.e_likizo.administrative.service;

import com.abe.e_likizo.administrative.dto.StationRequest;
import com.abe.e_likizo.administrative.dto.StationResponse;
import com.abe.e_likizo.administrative.entity.District;
import com.abe.e_likizo.administrative.entity.Station;
import com.abe.e_likizo.administrative.repository.DistrictRepository;
import com.abe.e_likizo.administrative.repository.StationRepository;
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
public class StationService {

    private final StationRepository stationRepository;
    private final DistrictRepository districtRepository;

    // CREATE
    public StationResponse create(StationRequest request) {

        String code = normalizeCode(request.code());

        if (stationRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii ya Station tayari imetumika"
            );
        }

        District district = findDistrict(request.districtId());

        Station station = new Station();
        station.setName(request.name().trim());
        station.setCode(code);
        station.setDescription(
                normalizeDescription(request.description())
        );
        station.setActive(
                request.active() == null || request.active()
        );
        station.setDistrict(district);

        Station saved = stationRepository.save(station);

        return toResponse(saved);
    }

    // READ ALL
    @Transactional(readOnly = true)
    public List<StationResponse> getAll() {

        return stationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // READ BY ID
    @Transactional(readOnly = true)
    public StationResponse getById(Long id) {

        return toResponse(findStation(id));
    }

    // READ BY DISTRICT
    @Transactional(readOnly = true)
    public List<StationResponse> getByDistrictId(Long districtId) {

        findDistrict(districtId);

        return stationRepository.findByDistrictId(districtId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // UPDATE
    public StationResponse update(
            Long id,
            StationRequest request
    ) {

        Station station = findStation(id);

        String code = normalizeCode(request.code());

        if (stationRepository.existsByCodeAndIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code hii inatumiwa na Station nyingine"
            );
        }

        District district = findDistrict(request.districtId());

        station.setName(request.name().trim());
        station.setCode(code);
        station.setDescription(
                normalizeDescription(request.description())
        );
        station.setDistrict(district);

        if (request.active() != null) {
            station.setActive(request.active());
        }

        Station updated = stationRepository.save(station);

        return toResponse(updated);
    }

    // DELETE
    public void delete(Long id) {

        Station station = findStation(id);

        stationRepository.delete(station);
    }

    // FIND STATION
    private Station findStation(Long id) {

        return stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Station haijapatikana kwa ID: " + id
                ));
    }

    // FIND DISTRICT
    private District findDistrict(Long id) {

        return districtRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "District haijapatikana kwa ID: " + id
                ));
    }

    // NORMALIZE CODE
    private String normalizeCode(String code) {

        return code.trim().toUpperCase(Locale.ROOT);
    }

    // NORMALIZE DESCRIPTION
    private String normalizeDescription(String description) {

        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    // MAP ENTITY TO RESPONSE
    private StationResponse toResponse(Station station) {

        return new StationResponse(
                station.getId(),
                station.getName(),
                station.getCode(),
                station.getDescription(),
                station.isActive(),
                station.getDistrict().getId(),
                station.getDistrict().getName(),
                station.getCreatedAt(),
                station.getCreatedBy(),
                station.getUpdatedAt(),
                station.getUpdatedBy()
        );
    }
}
