package com.uptrix.uptrix_backend.repository.helpdesk;

import com.uptrix.uptrix_backend.entity.helpdesk.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {

    // OLD (❌) – no ticketId field on entity
    // List<TicketAttachment> findByTicketIdAndCompanyId(Long ticketId, Long companyId);

    // ✅ NEW – use property path: ticket.id → ticket_Id
    List<TicketAttachment> findByTicket_IdAndCompanyId(Long ticketId, Long companyId);
}
