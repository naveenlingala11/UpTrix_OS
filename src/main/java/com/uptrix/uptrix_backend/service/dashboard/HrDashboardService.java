package com.uptrix.uptrix_backend.service.dashboard;

import com.uptrix.uptrix_backend.dto.company.DepartmentAttendanceSummaryDto;
import com.uptrix.uptrix_backend.dto.company.RecentLeaveDto;
import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.entity.Department;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.LeaveRequest;
import com.uptrix.uptrix_backend.repository.AttendanceRepository;
import com.uptrix.uptrix_backend.repository.DepartmentRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.LeaveRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HrDashboardService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public HrDashboardService(DepartmentRepository departmentRepository,
                              EmployeeRepository employeeRepository,
                              AttendanceRepository attendanceRepository,
                              LeaveRequestRepository leaveRequestRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<DepartmentAttendanceSummaryDto> getTodayDepartmentAttendance(Long companyId) {
        LocalDate today = LocalDate.now();

        // All departments for this company
        List<Department> departments = departmentRepository.findByCompanyId(companyId);

        // All employees (we'll group them by dept)
        List<Employee> allEmployees = employeeRepository.findByCompanyId(companyId);

        // deptId -> employees (0L for "no department")
        Map<Long, List<Employee>> byDept = allEmployees.stream()
                .collect(Collectors.groupingBy(e ->
                        e.getDepartment() != null && e.getDepartment().getId() != null
                                ? e.getDepartment().getId()
                                : 0L
                ));

        // Today attendance (Present)
        List<Attendance> todayAttendance = attendanceRepository.findByCompanyIdAndDate(companyId, today);
        Set<Long> presentEmployeeIds = todayAttendance.stream()
                .map(a -> a.getEmployee().getId())
                .collect(Collectors.toSet());

        // Today leaves (Approved & covers today)
        List<LeaveRequest> todayLeaves =
                leaveRequestRepository.findByCompanyIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        companyId, "APPROVED", today, today
                );
        Set<Long> onLeaveEmployeeIds = todayLeaves.stream()
                .map(l -> l.getEmployee().getId())
                .collect(Collectors.toSet());

        List<DepartmentAttendanceSummaryDto> result = new ArrayList<>();

        // Real departments
        for (Department dept : departments) {
            Long deptId = dept.getId();
            List<Employee> deptEmployees = byDept.getOrDefault(deptId, List.of());

            long totalActive = deptEmployees.stream()
                    .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                    .count();

            long present = deptEmployees.stream()
                    .filter(e -> presentEmployeeIds.contains(e.getId()))
                    .count();

            long onLeave = deptEmployees.stream()
                    .filter(e -> onLeaveEmployeeIds.contains(e.getId()))
                    .count();

            long unplannedAbsent = Math.max(0, totalActive - (present + onLeave));

            DepartmentAttendanceSummaryDto dto = new DepartmentAttendanceSummaryDto();
            dto.setDepartmentId(deptId);
            dto.setDepartmentName(dept.getName());
            dto.setTotalEmployees(totalActive);
            dto.setPresentCount(present);
            dto.setOnLeaveCount(onLeave);
            dto.setUnplannedAbsentCount(unplannedAbsent);

            result.add(dto);
        }

        // Employees with NO department
        List<Employee> unassigned = byDept.getOrDefault(0L, List.of());
        if (!unassigned.isEmpty()) {
            long totalActive = unassigned.stream()
                    .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                    .count();

            long present = unassigned.stream()
                    .filter(e -> presentEmployeeIds.contains(e.getId()))
                    .count();

            long onLeave = unassigned.stream()
                    .filter(e -> onLeaveEmployeeIds.contains(e.getId()))
                    .count();

            long unplannedAbsent = Math.max(0, totalActive - (present + onLeave));

            DepartmentAttendanceSummaryDto dto = new DepartmentAttendanceSummaryDto();
            dto.setDepartmentId(null);
            dto.setDepartmentName("No department");
            dto.setTotalEmployees(totalActive);
            dto.setPresentCount(present);
            dto.setOnLeaveCount(onLeave);
            dto.setUnplannedAbsentCount(unplannedAbsent);

            result.add(dto);
        }

        return result;
    }

    public List<RecentLeaveDto> getRecentLeaves(Long companyId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        Page<LeaveRequest> page = leaveRequestRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageRequest);

        return page.getContent().stream()
                .map(lr -> {
                    RecentLeaveDto dto = new RecentLeaveDto();
                    dto.setId(lr.getId());

                    String firstName = lr.getEmployee().getFirstName() != null ? lr.getEmployee().getFirstName() : "";
                    String lastName = lr.getEmployee().getLastName() != null ? lr.getEmployee().getLastName() : "";
                    dto.setEmployeeName((firstName + " " + lastName).trim());

                    if (lr.getEmployee().getDepartment() != null) {
                        dto.setDepartmentName(lr.getEmployee().getDepartment().getName());
                    }

                    dto.setLeaveType(lr.getLeaveType());
                    dto.setStartDate(lr.getStartDate());
                    dto.setEndDate(lr.getEndDate());
                    dto.setStatus(lr.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
