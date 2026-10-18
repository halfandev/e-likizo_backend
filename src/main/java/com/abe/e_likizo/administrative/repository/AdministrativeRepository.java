package com.abe.e_likizo.administrative.repository;

import com.abe.e_likizo.administrative.entity.Administrative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministrativeRepository
        extends JpaRepository<Administrative, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<Administrative> findByCode(String code);
}
