package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndCompanyId(String username, Long companyId);

    boolean existsByCompanyIdAndEmail(Long companyId, String email);

    List<User> findByCompanyId(Long companyId);

    /**
     * Count all users for a given company.
     */
    long countByCompanyId(Long companyId);
}
