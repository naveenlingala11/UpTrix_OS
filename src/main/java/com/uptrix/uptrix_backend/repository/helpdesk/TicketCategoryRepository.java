package com.uptrix.uptrix_backend.repository.helpdesk;

import com.uptrix.uptrix_backend.entity.helpdesk.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    // For createTicket()
    Optional<TicketCategory> findByIdAndCompanyId(Long id, Long companyId);

    // For dropdown list on UI
    List<TicketCategory> findByCompanyIdAndActiveTrueOrderByNameAsc(Long companyId);
}
