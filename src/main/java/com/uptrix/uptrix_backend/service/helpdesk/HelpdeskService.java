package com.uptrix.uptrix_backend.service.helpdesk;

import com.uptrix.uptrix_backend.config.FileStorageConfig;
import com.uptrix.uptrix_backend.dto.UserPrincipal;
import com.uptrix.uptrix_backend.dto.helpdesk.*;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.entity.helpdesk.Ticket;
import com.uptrix.uptrix_backend.entity.helpdesk.TicketAttachment;
import com.uptrix.uptrix_backend.entity.helpdesk.TicketCategory;
import com.uptrix.uptrix_backend.entity.helpdesk.TicketComment;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import com.uptrix.uptrix_backend.repository.helpdesk.TicketAttachmentRepository;
import com.uptrix.uptrix_backend.repository.helpdesk.TicketCategoryRepository;
import com.uptrix.uptrix_backend.repository.helpdesk.TicketCommentRepository;
import com.uptrix.uptrix_backend.repository.helpdesk.TicketRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.uptrix.uptrix_backend.security.SecurityUtils.getCurrentUser;

@Service
@Transactional
public class HelpdeskService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketCategoryRepository categoryRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final FileStorageConfig fileStorageConfig;
    private final UserRepository userRepository;   // ✅ NEW

    public HelpdeskService(TicketRepository ticketRepository,
                           TicketCommentRepository commentRepository,
                           TicketCategoryRepository categoryRepository,
                           EmployeeRepository employeeRepository,
                           TicketAttachmentRepository attachmentRepository,
                           FileStorageConfig fileStorageConfig,
                           UserRepository userRepository) {   // ✅ NEW
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.categoryRepository = categoryRepository;
        this.employeeRepository = employeeRepository;
        this.attachmentRepository = attachmentRepository;
        this.fileStorageConfig = fileStorageConfig;
        this.userRepository = userRepository;       // ✅ NEW
    }


    private String fullName(Employee e) {
        if (e == null) return null;
        String first = e.getFirstName() != null ? e.getFirstName() : "";
        String last = e.getLastName() != null ? e.getLastName() : "";
        String name = (first + " " + last).trim();
        return name.isEmpty() ? null : name;
    }

    /* ======================= create ticket ======================= */

    public TicketDetailDto createTicket(Long companyId, CreateTicketRequest req) {

        UserPrincipal principal = getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("No authenticated user"));

        // ✅ Robust resolution for current employee
        Employee employee = resolveEmployeeForCurrentUser(companyId, principal);

        TicketCategory category = categoryRepository
                .findByIdAndCompanyId(req.getCategoryId(), companyId)
                .orElseThrow(() -> new RuntimeException("Category not found for this company"));

        Ticket ticket = new Ticket();
        ticket.setCompanyId(companyId);
        ticket.setEmployee(employee);
        ticket.setCategory(category);
        ticket.setSubject(req.getSubject());
        ticket.setDescription(req.getDescription());
        ticket.setPriority(req.getPriority());
        ticket.setStatus("OPEN");
        ticket.setSource("PORTAL");
        ticket.setVisibilityScope("EMPLOYEE_HR");

        LocalDateTime now = LocalDateTime.now();
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        if (category.getDefaultSlaHours() != null) {
            ticket.setSlaDueAt(now.plusHours(category.getDefaultSlaHours()));
        }

        if (category.getDefaultOwner() != null) {
            ticket.setHrOwner(category.getDefaultOwner());
        }

        ticketRepository.save(ticket);

        return mapToDetail(ticket, Collections.emptyList());
    }

    /**
     * ✅ Resolve Employee for current user with auto-link / auto-create:
     * 1) principal.employeeId (from JWT)
     * 2) user.getEmployee()
     * 3) Employee by (companyId, workEmail)
     * 4) Auto-create Employee from User if nothing exists
     */
    private Employee resolveEmployeeForCurrentUser(Long companyId, UserPrincipal principal) {

        // 1) If JWT already has employeeId → use it
        Long employeeId = principal.getEmployeeId();
        if (employeeId != null) {
            return employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                    .orElseThrow(() -> new AccessDeniedException("Employee not found for this company"));
        }

        // 2) Load User
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Prefer strong mapping: User.employee
        if (user.getEmployee() != null) {
            Employee emp = user.getEmployee();
            if (emp.getCompany() != null && !emp.getCompany().getId().equals(companyId)) {
                throw new AccessDeniedException("Employee belongs to a different company");
            }
            return emp;
        }

        // 3) Try existing Employee by workEmail + company
        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            Optional<Employee> maybeEmp = employeeRepository
                    .findFirstByCompanyIdAndWorkEmail(companyId, email);

            if (maybeEmp.isPresent()) {
                Employee emp = maybeEmp.get();

                // Link back to User if not already
                if (emp.getUser() == null) {
                    emp.setUser(user);
                    employeeRepository.save(emp);
                }
                if (user.getEmployee() == null) {
                    user.setEmployee(emp);
                    userRepository.save(user);
                }

                return emp;
            }
        }

        // 4) Nothing found → auto-create minimal Employee and link
        Employee newEmp = new Employee();
        newEmp.setCompany(user.getCompany());
        newEmp.setEmployeeCode("EMP-" + user.getId());   // unique per company
        // Split name roughly: firstName = fullName, you can refine later
        String fullName = user.getFullName();
        if (fullName == null || fullName.isBlank()) {
            newEmp.setFirstName(user.getUsername());
        } else {
            newEmp.setFirstName(fullName);
        }
        newEmp.setLastName(null);
        newEmp.setWorkEmail(user.getEmail());
        newEmp.setStatus("ACTIVE");

        newEmp = employeeRepository.save(newEmp);

        user.setEmployee(newEmp);
        userRepository.save(user);

        return newEmp;
    }


    // ---------------- CATEGORIES ----------------

    @Transactional(readOnly = true)
    public List<TicketCategoryDto> getActiveCategories(Long companyId) {
        List<TicketCategory> categories =
                categoryRepository.findByCompanyIdAndActiveTrueOrderByNameAsc(companyId);

        return categories.stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    private TicketCategoryDto mapToCategoryDto(TicketCategory entity) {
        TicketCategoryDto dto = new TicketCategoryDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setDefaultSlaHours(entity.getDefaultSlaHours());
        return dto;
    }

    // ---------------- OLD LIST METHODS (still used for /tickets) ----------------

    @Transactional(readOnly = true)
    public Page<TicketSummaryDto> listTicketsForEmployee(Long companyId,
                                                         Long employeeId,
                                                         int page,
                                                         int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Ticket> tickets = ticketRepository
                .findByCompanyIdAndEmployee_IdOrderByCreatedAtDesc(companyId, employeeId, pageable);

        return tickets.map(this::mapToSummary);
    }

    @Transactional(readOnly = true)
    public Page<TicketSummaryDto> listTicketsForHr(Long companyId,
                                                   Long userId,
                                                   int page,
                                                   int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Ticket> tickets = ticketRepository
                .findByCompanyIdOrderByCreatedAtDesc(companyId, pageable);

        return tickets.map(this::mapToSummary);
    }

    // ---------------- GET TICKET ----------------

    @Transactional(readOnly = true)
    public TicketDetailDto getTicket(Long companyId, Long ticketId) {
        Ticket ticket = ticketRepository.findByIdAndCompanyId(ticketId, companyId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        List<TicketComment> comments = commentRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId);

        return mapToDetail(ticket, comments);
    }

    // ---------------- ADD COMMENT ----------------

    public TicketDetailDto addComment(Long companyId,
                                      Long ticketId,
                                      AddCommentRequest req) {

        if (req == null) {
            throw new IllegalArgumentException("Comment payload cannot be null");
        }

        String rawMessage = req.getMessage();
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment message cannot be empty");
        }
        String message = rawMessage.trim();

        Ticket ticket = ticketRepository.findByIdAndCompanyId(ticketId, companyId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        LocalDateTime now = LocalDateTime.now();
        boolean internal = Boolean.TRUE.equals(req.getInternal());

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setMessage(message);
        comment.setInternal(internal);
        comment.setCreatedAt(now);

        if (internal) {
            if (ticket.getHrOwner() != null) {
                StringBuilder name = new StringBuilder();
                if (ticket.getHrOwner().getFirstName() != null) {
                    name.append(ticket.getHrOwner().getFirstName());
                }
                if (ticket.getHrOwner().getLastName() != null) {
                    if (name.length() > 0) {
                        name.append(" ");
                    }
                    name.append(ticket.getHrOwner().getLastName());
                }
                comment.setAuthorName(
                        name.length() > 0 ? name.toString() : "HR"
                );
            } else {
                comment.setAuthorName("HR Team");
            }
            comment.setAuthorRole("HR");
        } else {
            if (ticket.getEmployee() != null) {
                StringBuilder name = new StringBuilder();
                if (ticket.getEmployee().getFirstName() != null) {
                    name.append(ticket.getEmployee().getFirstName());
                }
                if (ticket.getEmployee().getLastName() != null) {
                    if (name.length() > 0) {
                        name.append(" ");
                    }
                    name.append(ticket.getEmployee().getLastName());
                }
                comment.setAuthorName(
                        name.length() > 0 ? name.toString() : "Employee"
                );
            } else {
                comment.setAuthorName("Employee");
            }
            comment.setAuthorRole("EMPLOYEE");
        }

        commentRepository.save(comment);

        if (internal && ticket.getStatus() != null
                && "OPEN".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
        }

        if (internal && ticket.getCategory() != null
                && ticket.getCategory().getDefaultSlaHours() != null) {

            Integer slaHours = ticket.getCategory().getDefaultSlaHours();

            if (ticket.getSlaDueAt() == null || ticket.getSlaDueAt().isBefore(now)) {
                ticket.setSlaDueAt(now.plusHours(slaHours));
            }
        }

        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        List<TicketComment> comments =
                commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        return mapToDetail(ticket, comments);
    }

    // ---------------- UPDATE TICKET ----------------

    public TicketDetailDto updateTicket(Long companyId,
                                        Long ticketId,
                                        UpdateTicketRequest req) {

        Ticket ticket = ticketRepository.findByIdAndCompanyId(ticketId, companyId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            ticket.setStatus(req.getStatus());
        }

        if (req.getPriority() != null && !req.getPriority().isBlank()) {
            ticket.setPriority(req.getPriority());
        }

        if (req.getHrOwnerId() != null) {
            if (req.getHrOwnerId() == 0L) {
                ticket.setHrOwner(null);
            } else {
                Employee owner = employeeRepository
                        .findByIdAndCompanyId(req.getHrOwnerId(), companyId)
                        .orElseThrow(() -> new RuntimeException("HR owner not found"));
                ticket.setHrOwner(owner);
            }
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        List<TicketComment> comments =
                commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        return mapToDetail(ticket, comments);
    }

    // ---------------- SATISFACTION ----------------

    public TicketDetailDto setSatisfaction(Long companyId,
                                           Long ticketId,
                                           int score) {
        Ticket ticket = ticketRepository.findByIdAndCompanyId(ticketId, companyId)
                .orElseThrow(() -> new RuntimeException("Ticket not found for this company"));

        if (score < 1) score = 1;
        if (score > 5) score = 5;

        ticket.setSatisfactionScore(score);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        List<TicketComment> comments =
                commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());

        return mapToDetail(ticket, comments);
    }

    // ---------------- ROLE-BASED getMyTickets ----------------

    @Transactional(readOnly = true)
    public Page<TicketSummaryDto> getMyTickets(Long companyId,
                                               int page,
                                               int size) {

        UserPrincipal user = getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("No authenticated user"));

        Long employeeId = user.getEmployeeId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (user.hasRole("ADMIN")) {
            Page<Ticket> tickets = ticketRepository
                    .findByCompanyIdOrderByCreatedAtDesc(companyId, pageable);
            return tickets.map(this::mapToSummary);
        }

        if (user.hasRole("HR") && employeeId != null) {
            return ticketRepository
                    .findByCompanyIdAndHrOwner_IdOrCompanyIdAndEmployee_IdOrderByCreatedAtDesc(
                            companyId, employeeId,
                            companyId, employeeId,
                            pageable
                    ).map(this::mapToSummary);
        }

        if (user.hasRole("MANAGER") && employeeId != null) {

            List<Employee> team = employeeRepository
                    .findByCompanyIdAndManager_Id(companyId, employeeId);

            Set<Long> employeeIds = new HashSet<>();
            employeeIds.add(employeeId);
            for (Employee e : team) {
                employeeIds.add(e.getId());
            }

            Page<Ticket> tickets = ticketRepository
                    .findByCompanyIdAndEmployee_IdInOrderByCreatedAtDesc(companyId, employeeIds, pageable);

            return tickets.map(this::mapToSummary);
        }

        if (employeeId == null) {
            return Page.empty(pageable);
        }

        Page<Ticket> tickets = ticketRepository
                .findByCompanyIdAndEmployee_IdOrderByCreatedAtDesc(companyId, employeeId, pageable);

        return tickets.map(this::mapToSummary);
    }

    // ---------------- MAPPERS & ATTACHMENTS ----------------

    private TicketSummaryDto mapToSummary(Ticket ticket) {
        TicketSummaryDto dto = new TicketSummaryDto();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());

        if (ticket.getCategory() != null) {
            dto.setCategoryName(ticket.getCategory().getName());
        }
        if (ticket.getEmployee() != null) {
            dto.setEmployeeName(fullName(ticket.getEmployee()));
        }
        if (ticket.getHrOwner() != null) {
            dto.setHrOwnerName(fullName(ticket.getHrOwner()));
        }

        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setSlaDueAt(ticket.getSlaDueAt());

        return dto;
    }

    private TicketCommentDto toCommentDto(TicketComment c) {
        TicketCommentDto cd = new TicketCommentDto();
        cd.setId(c.getId());
        cd.setAuthorName(c.getAuthorName());
        cd.setAuthorRole(c.getAuthorRole());
        cd.setInternal(c.isInternal());
        cd.setMessage(c.getMessage());
        cd.setCreatedAt(c.getCreatedAt());
        return cd;
    }

    private TicketAttachmentDto toAttachmentDto(TicketAttachment a) {
        TicketAttachmentDto dto = new TicketAttachmentDto();
        dto.setId(a.getId());
        dto.setOriginalName(a.getOriginalName());
        dto.setContentType(a.getContentType());
        dto.setFileSize(a.getFileSize());
        dto.setUploadedAt(a.getUploadedAt());
        return dto;
    }

    private TicketDetailDto mapToDetail(Ticket ticket, List<TicketComment> comments) {
        TicketDetailDto dto = new TicketDetailDto();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());

        if (ticket.getCategory() != null) {
            dto.setCategoryId(ticket.getCategory().getId());
            dto.setCategoryName(ticket.getCategory().getName());
        }

        if (ticket.getEmployee() != null) {
            dto.setEmployeeId(ticket.getEmployee().getId());
            dto.setEmployeeName(fullName(ticket.getEmployee()));
        }

        if (ticket.getHrOwner() != null) {
            dto.setHrOwnerId(ticket.getHrOwner().getId());
            dto.setHrOwnerName(fullName(ticket.getHrOwner()));
        }

        dto.setSource(ticket.getSource());
        dto.setVisibilityScope(ticket.getVisibilityScope());
        dto.setSlaDueAt(ticket.getSlaDueAt());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setSatisfactionScore(ticket.getSatisfactionScore());

        List<TicketCommentDto> commentDtos = comments.stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
        dto.setComments(commentDtos);

        List<TicketAttachment> ats =
                attachmentRepository.findByTicket_IdAndCompanyId(ticket.getId(), ticket.getCompanyId());

        List<TicketAttachmentDto> attDtos = ats.stream()
                .map(this::toAttachmentDto)
                .collect(Collectors.toList());
        dto.setAttachments(attDtos);

        return dto;
    }

    @Transactional
    public List<TicketAttachmentDto> uploadAttachments(Long companyId,
                                                       Long ticketId,
                                                       List<MultipartFile> files) {

        Ticket ticket = ticketRepository
                .findByIdAndCompanyId(ticketId, companyId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        List<TicketAttachmentDto> result = new ArrayList<>();

        Path uploadRoot = fileStorageConfig.getUploadRoot();
        Path dir = uploadRoot
                .resolve("company-" + companyId)
                .resolve("ticket-" + ticketId);

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create attachment directory: " + dir, e);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            try {
                TicketAttachment entity = new TicketAttachment();
                entity.setCompanyId(companyId);
                entity.setTicket(ticket);
                entity.setOriginalName(file.getOriginalFilename());
                entity.setContentType(file.getContentType());
                entity.setFileSize(file.getSize());
                entity.setUploadedAt(LocalDateTime.now());

                String storedName = UUID.randomUUID() + "-" + file.getOriginalFilename();
                Path target = dir.resolve(storedName);

                Files.copy(file.getInputStream(), target);

                entity.setStoragePath(target.toString());

                TicketAttachment saved = attachmentRepository.save(entity);
                result.add(toAttachmentDto(saved));

            } catch (IOException ex) {
                throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), ex);
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAttachment(Long companyId,
                                                       Long ticketId,
                                                       Long attachmentId) {

        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        if (!attachment.getCompanyId().equals(companyId) ||
                !attachment.getTicket().getId().equals(ticketId)) {
            throw new RuntimeException("Attachment does not belong to this ticket/company");
        }

        Path path = Paths.get(attachment.getStoragePath());
        if (!Files.exists(path)) {
            throw new RuntimeException("File not found on disk");
        }

        Resource resource = new FileSystemResource(path);
        String fileName = attachment.getOriginalName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(
                        Optional.ofNullable(attachment.getContentType())
                                .orElse("application/octet-stream")))
                .contentLength(attachment.getFileSize())
                .body(resource);
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getTicketDetail(Long companyId, Long ticketId) {
        return getTicket(companyId, ticketId);
    }
}
