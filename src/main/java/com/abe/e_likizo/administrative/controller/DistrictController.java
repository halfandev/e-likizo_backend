
package com.abe.e_likizo.administrative.controller;

import com.abe.e_likizo.administrative.dto.DistrictRequest;
import com.abe.e_likizo.administrative.dto.DistrictResponse;
import com.abe.e_likizo.administrative.service.DistrictService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    // CREATE
    @PostMapping
    public ResponseEntity<DistrictResponse> create(
            @Valid @RequestBody DistrictRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(districtService.create(request));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<DistrictResponse>> getAll() {

        return ResponseEntity.ok(districtService.getAll());
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<DistrictResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(districtService.getById(id));
    }

    // FILTER BY REGION
    @GetMapping("/by-region/{regionId}")
    public ResponseEntity<List<DistrictResponse>> getByRegionId(
            @PathVariable Long regionId) {

        return ResponseEntity.ok(
                districtService.getByRegionId(regionId)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<DistrictResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DistrictRequest request) {

        return ResponseEntity.ok(
                districtService.update(id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        districtService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
