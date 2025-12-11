package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.AttendanceDto;
import com.uptrix.uptrix_backend.dto.leave.LeaveRequestDto;
import com.uptrix.uptrix_backend.dto.SelfProfileDto;
import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.LeaveRequest;
import com.uptrix.uptrix_backend.entity.Role;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.AttendanceRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.LeaveRequestRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SelfService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public SelfService(EmployeeRepository employeeRepository,
                       LeaveRequestRepository leaveRequestRepository,
                       AttendanceRepository attendanceRepository,
                       UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context");
        }

        Object principal = auth.getPrincipal();
        String username;

        if (principal instanceof User u) {
            // principal-lo unna User detached untundi
            // kabatti DB nunchi fresh managed entity ni theeskundam
            return userRepository.findById(u.getId())
                    .orElseThrow(() -> new IllegalStateException("User not found: id=" + u.getId()));
        } else if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else if (principal instanceof String s) {
            username = s;
        } else {
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    private Employee getCurrentEmployeeOrThrow(User user) {
        return employeeRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("No employee profile linked to this user"));
    }

    public SelfProfileDto getProfile() {
        User user = getCurrentUserOrThrow();
        Employee employee = null;
        try {
            employee = getCurrentEmployeeOrThrow(user);
        } catch (Exception ignored) {
        }

        SelfProfileDto dto = new SelfProfileDto();
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse(null);
        dto.setPrimaryRole(primaryRole);

        dto.setCompanyId(user.getCompany().getId());
        dto.setCompanyName(user.getCompany().getName());

        if (employee != null) {
            dto.setEmployeeId(employee.getId());
            dto.setEmployeeCode(employee.getEmployeeCode());
            dto.setEmploymentType(employee.getEmploymentType());
            dto.setStatus(employee.getStatus());
            if (employee.getDepartment() != null) {
                dto.setDepartmentName(employee.getDepartment().getName());
            }
        }

        return dto;
    }

    /**
     * Returns leaves for current user if they have an Employee profile.
     * If not, returns empty list instead of throwing.
     */
    public List<LeaveRequestDto> getMyLeaves() {
        User user = getCurrentUserOrThrow();

        return employeeRepository.findByUser(user)
                .map(employee -> leaveRequestRepository
                        .findByCompanyIdAndEmployeeId(user.getCompany().getId(), employee.getId())
                        .stream()
                        .map(this::toLeaveDto)
                        .toList())
                .orElse(List.of());
    }

    /**
     * Returns attendance history for current user if they have an Employee profile.
     * If not, returns empty list.
     */
    public List<AttendanceDto> getMyAttendanceHistory(int limit) {
        User user = getCurrentUserOrThrow();

        return employeeRepository.findByUser(user)
                .map(employee -> {
                    Pageable pageable = PageRequest.of(0, limit);
                    return attendanceRepository
                            .findByCompanyIdAndEmployeeIdOrderByDateDesc(
                                    user.getCompany().getId(), employee.getId(), pageable
                            )
                            .stream()
                            .map(this::toAttendanceDto)
                            .toList();
                })
                .orElse(List.of());
    }

    private LeaveRequestDto toLeaveDto(LeaveRequest leave) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployee().getId());
        dto.setEmployeeCode(leave.getEmployee().getEmployeeCode());
        dto.setEmployeeName(
                leave.getEmployee().getFirstName()
                        + (leave.getEmployee().getLastName() != null ? " " + leave.getEmployee().getLastName() : "")
        );
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setLeaveType(leave.getLeaveType());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setCreatedAt(leave.getCreatedAt());
        return dto;
    }

    private AttendanceDto toAttendanceDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeCode(a.getEmployee().getEmployeeCode());
        dto.setEmployeeName(
                a.getEmployee().getFirstName()
                        + (a.getEmployee().getLastName() != null ? " " + a.getEmployee().getLastName() : "")
        );
        dto.setDate(a.getDate());
        dto.setCheckInTime(a.getCheckInTime());
        dto.setCheckOutTime(a.getCheckOutTime());
        dto.setStatus(a.getStatus());
        return dto;
    }

}
