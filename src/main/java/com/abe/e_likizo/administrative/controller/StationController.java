
package com.abe.e_likizo.administrative.controller;

import com.abe.e_likizo.administrative.dto.StationRequest;
import com.abe.e_likizo.administrative.dto.StationResponse;
import com.abe.e_likizo.administrative.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse create(
            @Valid @RequestBody StationRequest request
    ) {
        return stationService.create(request);
    }

    // READ ALL
    @GetMapping
    public List<StationResponse> getAll() {

        return stationService.getAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public StationResponse getById(@PathVariable Long id) {

        return stationService.getById(id);
    }

    // READ BY DISTRICT
    @GetMapping("/by-district/{districtId}")
    public List<StationResponse> getByDistrictId(
            @PathVariable Long districtId
    ) {
        return stationService.getByDistrictId(districtId);
    }

    // UPDATE
    @PutMapping("/{id}")
    public StationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody StationRequest request
    ) {
        return stationService.update(id, request);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        stationService.delete(id);
    }
}
