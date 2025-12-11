package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findBySubdomain(String subdomain);

    boolean existsByName(String name);

    boolean existsBySubdomain(String subdomain);

    long countByStatus(String status);

    /**
     * Tenants ordered by most recently updated (proxy for activity).
     */
    List<Company> findTop10ByOrderByUpdatedAtDesc();

    /**
     * Fallback ordering by ID (if you ever need it).
     */
    List<Company> findTop10ByOrderByIdDesc();

    /**
     * For alerts (e.g. TRIAL tenants).
     */
    List<Company> findByStatus(String status);
}
