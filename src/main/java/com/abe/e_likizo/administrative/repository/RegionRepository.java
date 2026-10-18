package com.abe.e_likizo.administrative.repository;

import com.abe.e_likizo.administrative.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Region> findByAdministrativeId(Long administrativeId);
}
