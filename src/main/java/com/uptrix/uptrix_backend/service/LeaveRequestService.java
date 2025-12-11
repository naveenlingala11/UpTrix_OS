package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.leave.LeaveRequestCreateDto;
import com.uptrix.uptrix_backend.dto.leave.LeaveRequestDto;
import com.uptrix.uptrix_backend.dto.leave.LeaveStatusUpdateDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.LeaveRequest;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.LeaveRequestRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                               CompanyRepository companyRepository,
                               EmployeeRepository employeeRepository,
                               NotificationService notificationService,
                               AuditLogService auditLogService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public LeaveRequestDto createLeave(Long companyId, LeaveRequestCreateDto request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee id"));

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start and end date are required");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setCompany(company);
        leave.setEmployee(employee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> findByCompany(Long companyId) {
        return leaveRequestRepository
                .findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toDto)
                .toList();

    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> listByCompany(Long companyId) {
        return leaveRequestRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "companyStats", key = "#companyId")
    public LeaveRequestDto updateStatus(Long companyId, Long leaveId, LeaveStatusUpdateDto req) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid leave id"));

        if (!leave.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Leave does not belong to this company");
        }

        String status = req.getStatus();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status) && !"PENDING".equals(status)) {
            throw new IllegalArgumentException("Invalid status");
        }

        leave.setStatus(status);

        if (!"PENDING".equals(status)) {
            User current = getCurrentUserOrThrow();
            leave.setDecidedBy(current);
            leave.setDecidedAt(LocalDateTime.now());
            leave.setDecisionComment(req.getComment());
        } else {
            leave.setDecidedBy(null);
            leave.setDecidedAt(null);
            leave.setDecisionComment(null);
        }

        LeaveRequest saved = leaveRequestRepository.save(leave);

        User employeeUser = saved.getEmployee().getUser();
        if (employeeUser != null) {
            String title;
            String type;

            switch (status) {
                case "APPROVED" -> {
                    title = "Leave approved";
                    type = "SUCCESS";
                }
                case "REJECTED" -> {
                    title = "Leave rejected";
                    type = "WARNING";
                }
                default -> {
                    title = "Leave updated";
                    type = "INFO";
                }
            }

            String baseMsg = "Your leave from %s to %s is now %s"
                    .formatted(saved.getStartDate(), saved.getEndDate(), status);

            if (req.getComment() != null && !req.getComment().isBlank()) {
                baseMsg += ". Note: " + req.getComment();
            }

            notificationService.createForUser(
                    employeeUser,
                    title,
                    baseMsg,
                    type,
                    "LEAVE_REQUEST",
                    saved.getId()
            );
        }

        String details = "Status changed to " + status;
        if (req.getComment() != null && !req.getComment().isBlank()) {
            details += " / Comment: " + req.getComment();
        }

        auditLogService.log(
                "LEAVE_STATUS_CHANGED",
                "LEAVE_REQUEST",
                saved.getId(),
                details
        );

        return toDto(saved);
    }

    private LeaveRequestDto toDto(LeaveRequest leave) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployee().getId());
        dto.setEmployeeCode(leave.getEmployee().getEmployeeCode());
        dto.setEmployeeName(
                leave.getEmployee().getFirstName() +
                        (leave.getEmployee().getLastName() != null ? " " + leave.getEmployee().getLastName() : "")
        );
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setLeaveType(leave.getLeaveType());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setCreatedAt(leave.getCreatedAt());

        if (leave.getDecidedBy() != null) {
            dto.setDecidedByName(leave.getDecidedBy().getFullName());
        }
        dto.setDecidedAt(leave.getDecidedAt());
        dto.setDecisionComment(leave.getDecisionComment());

        return dto;
    }

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("No authenticated user");
        }
        return user;
    }

}
