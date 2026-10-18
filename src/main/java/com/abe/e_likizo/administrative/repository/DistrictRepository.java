
package com.abe.e_likizo.administrative.repository;

import com.abe.e_likizo.administrative.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository
        extends JpaRepository<District, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<District> findByRegionId(Long regionId);
}
