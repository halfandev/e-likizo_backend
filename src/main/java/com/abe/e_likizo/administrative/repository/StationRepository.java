
package com.abe.e_likizo.administrative.repository;

import com.abe.e_likizo.administrative.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository
        extends JpaRepository<Station, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Station> findByDistrictId(Long districtId);
}
