package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByStatus(String status);

    boolean existsByCode(String code);

    Optional<Shift> findByCodeAndStatus(String code, String status);

    List<Shift> findByRotationGroupOrderByRotationOrderAsc(String rotationGroup);
}
