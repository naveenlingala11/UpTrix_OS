package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.payroll.TaxSlab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxSlabRepository extends JpaRepository<TaxSlab, Long> {

    List<TaxSlab> findByCompanyIdAndActiveTrueOrderByFromAmountAsc(Long companyId);

    boolean existsByCompanyIdAndActiveTrue(Long companyId);
}
