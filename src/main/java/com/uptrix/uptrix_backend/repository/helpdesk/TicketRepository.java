package com.uptrix.uptrix_backend.repository.helpdesk;

import com.uptrix.uptrix_backend.entity.helpdesk.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByIdAndCompanyId(Long id, Long companyId);

    Page<Ticket> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    Page<Ticket> findByCompanyIdAndEmployee_IdOrderByCreatedAtDesc(
            Long companyId,
            Long employeeId,
            Pageable pageable
    );

    Page<Ticket> findByCompanyIdAndEmployee_IdInOrderByCreatedAtDesc(
            Long companyId,
            Collection<Long> employeeIds,
            Pageable pageable
    );

    Page<Ticket> findByCompanyIdAndHrOwner_IdOrCompanyIdAndEmployee_IdOrderByCreatedAtDesc(
            Long companyId1,
            Long hrOwnerId,
            Long companyId2,
            Long employeeId,
            Pageable pageable
    );
}
