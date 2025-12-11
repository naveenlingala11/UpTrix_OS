package com.uptrix.uptrix_backend.service.shift;

import com.uptrix.uptrix_backend.dto.shift.EmployeeShiftAssignRequest;
import com.uptrix.uptrix_backend.dto.shift.ShiftChangeDecisionDto;
import com.uptrix.uptrix_backend.dto.shift.ShiftChangeRequestDto;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.EmployeeShiftAssignment;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.entity.ShiftChangeRequest;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.EmployeeShiftAssignmentRepository;
import com.uptrix.uptrix_backend.repository.ShiftChangeRequestRepository;
import com.uptrix.uptrix_backend.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeShiftService {

    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final ShiftChangeRequestRepository shiftChangeRequestRepository;

    public EmployeeShiftService(EmployeeRepository employeeRepository,
                                ShiftRepository shiftRepository,
                                EmployeeShiftAssignmentRepository assignmentRepository,
                                ShiftChangeRequestRepository shiftChangeRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.assignmentRepository = assignmentRepository;
        this.shiftChangeRequestRepository = shiftChangeRequestRepository;
    }

    @Transactional
    public EmployeeShiftAssignment assignShift(EmployeeShiftAssignRequest req) {
        if (req == null || req.getEmployeeId() == null || req.getShiftId() == null) {
            throw new IllegalArgumentException("employeeId and shiftId are required");
        }
        if (!StringUtils.hasText(req.getEffectiveFrom())) {
            throw new IllegalArgumentException("effectiveFrom is required");
        }

        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Shift shift = shiftRepository.findById(req.getShiftId())
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));

        LocalDate from = LocalDate.parse(req.getEffectiveFrom());
        LocalDate to = StringUtils.hasText(req.getEffectiveTo())
                ? LocalDate.parse(req.getEffectiveTo())
                : null;

        LocalDate overlapEnd = (to != null) ? to : from;

        // Conflict detection – skip only for explicit overrides if you want.
        boolean isException = Boolean.TRUE.equals(req.getExceptionOverride());
        if (!isException) {
            long conflicts = assignmentRepository.countOverlappingAssignments(
                    employee.getId(), from, overlapEnd);
            if (conflicts > 0) {
                throw new IllegalArgumentException("Employee already has an active shift in this period");
            }
        }

        EmployeeShiftAssignment assignment = new EmployeeShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShift(shift);
        assignment.setEffectiveFrom(from);
        assignment.setEffectiveTo(to);
        assignment.setExceptionOverride(isException);
        assignment.setRotationSequence(req.getRotationSequence());
        assignment.setStatus("ACTIVE");

        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<EmployeeShiftAssignment> getAssignmentsForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return assignmentRepository.findByEmployee(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeShiftAssignment getActiveAssignmentForEmployeeOnDate(Long employeeId, LocalDate date) {
        List<EmployeeShiftAssignment> list =
                assignmentRepository.findActiveAssignmentsForEmployeeOnDate(employeeId, date);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Auto-shift fallback:
     * You can call this from AttendanceService if an employee has no shift
     * on a date and you want to assign a default one.
     */
    @Transactional
    public EmployeeShiftAssignment autoAssignDefaultShiftIfMissing(Long employeeId, LocalDate date, Long defaultShiftId) {
        EmployeeShiftAssignment existing = getActiveAssignmentForEmployeeOnDate(employeeId, date);
        if (existing != null) {
            return existing;
        }

        Shift defaultShift = shiftRepository.findById(defaultShiftId)
                .orElseThrow(() -> new IllegalArgumentException("Default shift not found"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        EmployeeShiftAssignment assignment = new EmployeeShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShift(defaultShift);
        assignment.setEffectiveFrom(date);
        assignment.setEffectiveTo(date);
        assignment.setExceptionOverride(Boolean.TRUE); // treat as auto override
        assignment.setStatus("ACTIVE");

        return assignmentRepository.save(assignment);
    }

    // -------------------- SHIFT CHANGE REQUESTS --------------------

    @Transactional
    public ShiftChangeRequest createShiftChangeRequest(ShiftChangeRequestDto dto) {
        if (dto == null || dto.getEmployeeId() == null ||
                dto.getFromShiftId() == null || dto.getToShiftId() == null) {
            throw new IllegalArgumentException("Invalid shift change request");
        }
        if (!StringUtils.hasText(dto.getEffectiveFrom())) {
            throw new IllegalArgumentException("effectiveFrom is required");
        }

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        Shift fromShift = shiftRepository.findById(dto.getFromShiftId())
                .orElseThrow(() -> new IllegalArgumentException("From shift not found"));
        Shift toShift = shiftRepository.findById(dto.getToShiftId())
                .orElseThrow(() -> new IllegalArgumentException("To shift not found"));

        LocalDate from = LocalDate.parse(dto.getEffectiveFrom());
        LocalDate to = StringUtils.hasText(dto.getEffectiveTo())
                ? LocalDate.parse(dto.getEffectiveTo())
                : null;

        ShiftChangeRequest scr = new ShiftChangeRequest();
        scr.setEmployee(employee);
        scr.setFromShift(fromShift);
        scr.setToShift(toShift);
        scr.setEffectiveFrom(from);
        scr.setEffectiveTo(to);
        scr.setReason(dto.getReason());
        scr.setStatus("PENDING");

        return shiftChangeRequestRepository.save(scr);
    }

    @Transactional
    public ShiftChangeRequest decideShiftChangeRequest(Long id, ShiftChangeDecisionDto decisionDto) {
        ShiftChangeRequest scr = shiftChangeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift change request not found"));

        if (!"PENDING".equals(scr.getStatus())) {
            throw new IllegalStateException("Only PENDING requests can be updated");
        }

        String action = decisionDto.getAction() == null
                ? null
                : decisionDto.getAction().trim().toUpperCase();

        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new IllegalArgumentException("action must be APPROVE or REJECT");
        }

        scr.setApproverUserId(decisionDto.getApproverUserId());
        scr.setApproverRemarks(decisionDto.getApproverRemarks());

        if ("REJECT".equals(action)) {
            scr.setStatus("REJECTED");
            return shiftChangeRequestRepository.save(scr);
        }

        // APPROVE: create an exception override assignment
        LocalDate from = scr.getEffectiveFrom();
        LocalDate to = scr.getEffectiveTo();
        LocalDate overlapEnd = (to != null) ? to : from;

        // Allow overlap because this is exceptionOverride = true
        EmployeeShiftAssignment assignment = new EmployeeShiftAssignment();
        assignment.setEmployee(scr.getEmployee());
        assignment.setShift(scr.getToShift());
        assignment.setEffectiveFrom(from);
        assignment.setEffectiveTo(to);
        assignment.setExceptionOverride(Boolean.TRUE);
        assignment.setStatus("ACTIVE");
        assignmentRepository.save(assignment);

        scr.setStatus("APPROVED");
        return shiftChangeRequestRepository.save(scr);
    }

    @Transactional(readOnly = true)
    public List<ShiftChangeRequest> getShiftChangeRequestsForEmployee(Long employeeId) {
        return shiftChangeRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<ShiftChangeRequest> getPendingShiftChangeRequests() {
        return shiftChangeRequestRepository.findByStatusOrderByCreatedAtAsc("PENDING");
    }
}
