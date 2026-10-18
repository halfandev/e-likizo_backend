package com.abe.e_likizo.administrative.controller;

import com.abe.e_likizo.administrative.administrative.AdministrativeResponse;
import com.abe.e_likizo.administrative.dto.AdministrativeRequest;
import com.abe.e_likizo.administrative.service.AdministrativeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/administratives")
@RequiredArgsConstructor
@CrossOrigin
public class AdministrativeController {

    private final AdministrativeService administrativeService;

    // CREATE
    @PostMapping
    public ResponseEntity<AdministrativeResponse> create(
            @Valid @RequestBody AdministrativeRequest request) {

        AdministrativeResponse response =
                administrativeService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<AdministrativeResponse>> getAll() {

        return ResponseEntity.ok(
                administrativeService.getAll()
        );
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<AdministrativeResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                administrativeService.getById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<AdministrativeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdministrativeRequest request) {

        return ResponseEntity.ok(
                administrativeService.update(id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        administrativeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
