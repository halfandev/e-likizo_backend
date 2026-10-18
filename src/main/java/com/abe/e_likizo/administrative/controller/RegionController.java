
package com.abe.e_likizo.administrative.controller;

import com.abe.e_likizo.administrative.dto.RegionRequest;
import com.abe.e_likizo.administrative.dto.RegionResponse;
import com.abe.e_likizo.administrative.service.RegionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    // CREATE
    @PostMapping
    public ResponseEntity<RegionResponse> create(
            @Valid @RequestBody RegionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(regionService.create(request));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<RegionResponse>> getAll() {

        return ResponseEntity.ok(regionService.getAll());
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<RegionResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(regionService.getById(id));
    }

    // FILTER BY ADMINISTRATIVE
    @GetMapping("/by-administrative/{administrativeId}")
    public ResponseEntity<List<RegionResponse>> getByAdministrativeId(
            @PathVariable Long administrativeId) {

        return ResponseEntity.ok(
                regionService.getByAdministrativeId(administrativeId)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<RegionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RegionRequest request) {

        return ResponseEntity.ok(
                regionService.update(id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        regionService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
