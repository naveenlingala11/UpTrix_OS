package com.uptrix.uptrix_backend.service.company;

import com.uptrix.uptrix_backend.dto.company.CompanyStatsDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CompanyStatsService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeShiftAssignmentRepository employeeShiftAssignmentRepository;

    public CompanyStatsService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            AttendanceRepository attendanceRepository,
            LeaveRequestRepository leaveRequestRepository,
            ShiftRepository shiftRepository,
            EmployeeShiftAssignmentRepository employeeShiftAssignmentRepository
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.shiftRepository = shiftRepository;
        this.employeeShiftAssignmentRepository = employeeShiftAssignmentRepository;
    }

    @Cacheable(cacheNames = "companyStats", key = "#companyId")
    @Transactional(readOnly = true)
    public CompanyStatsDto getStats(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        CompanyStatsDto dto = new CompanyStatsDto();
        dto.setCompanyId(company.getId());
        dto.setCompanyName(company.getName());

        long totalUsers = userRepository.findByCompanyId(companyId).size();
        long totalEmployees = employeeRepository.findByCompanyId(companyId).size();
        long totalDepartments = departmentRepository.findByCompanyId(companyId).size();

        dto.setTotalUsers(totalUsers);
        dto.setTotalEmployees(totalEmployees);
        dto.setTotalDepartments(totalDepartments);

        LocalDate today = LocalDate.now();

        // ✅ Attendance
        long presentToday = attendanceRepository.countByCompanyIdAndDate(companyId, today);
        dto.setTotalEmployeesPresentToday(presentToday);

        // ✅ Pending leaves
        long pendingLeaves = leaveRequestRepository.countByCompanyIdAndStatus(companyId, "PENDING");
        dto.setPendingLeaves(pendingLeaves);

        // ===============================
        // HR EXTRA METRICS
        // ===============================

        // 1️⃣ On Leave Today (APPROVED + overlapping)
        long onLeaveToday = leaveRequestRepository.countApprovedOverlappingDate(companyId, today);
        dto.setOnLeaveToday(onLeaveToday);

        // 2️⃣ Unplanned Absents = Employees - (Present + On Leave)
        long unplannedAbsent = totalEmployees - (presentToday + onLeaveToday);
        dto.setUnplannedAbsentToday(Math.max(unplannedAbsent, 0));

        LocalDate last30 = today.minusDays(30);
        LocalDateTime last30DateTime = last30.atStartOfDay();

        // 3️⃣ New Joinees in last 30 days
        long newJoineesLast30 = employeeRepository
                .countByCompanyIdAndDateOfJoiningAfter(companyId, last30);
        dto.setNewJoineesLast30(newJoineesLast30);

        // 4️⃣ Exited in last 30 days (status = EXITED)
        long exitedLast30 = employeeRepository
                .countByCompanyIdAndStatusAndUpdatedAtAfter(companyId, "EXITED", last30DateTime);
        dto.setExitedLast30(exitedLast30);

        // 5️⃣ Upcoming joiners (DOJ > today)
        long upcomingJoiners = employeeRepository
                .countByCompanyIdAndDateOfJoiningAfter(companyId, today);
        dto.setUpcomingJoiners(upcomingJoiners);

        // 6️⃣ Shifts info
        long totalShifts = shiftRepository.count();
        dto.setTotalShifts(totalShifts);

        long withShiftToday = employeeShiftAssignmentRepository
                .countEmployeesWithActiveShiftOnDate(companyId, today);
        dto.setEmployeesWithoutShift(Math.max(totalEmployees - withShiftToday, 0));

        long nightShiftHeadcount = employeeShiftAssignmentRepository
                .countEmployeesOnNightShiftOnDate(companyId, today);
        dto.setNightShiftHeadcount(nightShiftHeadcount);

        // 7️⃣ Probation – not yet in Employee => keep 0 for now
        dto.setEmployeesOnProbation(0L);

        return dto;
    }

}
