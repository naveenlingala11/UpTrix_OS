package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.ShiftChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftChangeRequestRepository extends JpaRepository<ShiftChangeRequest, Long> {

    List<ShiftChangeRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<ShiftChangeRequest> findByStatusOrderByCreatedAtAsc(String status);
}
