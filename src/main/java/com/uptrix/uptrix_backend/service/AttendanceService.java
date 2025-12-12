package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.AttendanceDto;
import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.repository.AttendanceRepository;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository,
                             CompanyRepository companyRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public AttendanceDto checkIn(Long companyId, Long employeeId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee"));

        LocalDate today = LocalDate.now();

        if (attendanceRepository
                .findByCompanyIdAndEmployeeIdAndDate(companyId, employeeId, today).isPresent()) {
            throw new IllegalArgumentException("Already checked in today");
        }

        Attendance attendance = new Attendance();
        attendance.setCompany(company);
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus("PRESENT");

        Attendance saved = attendanceRepository.save(attendance);
        return toDto(saved);
    }

    @Transactional
    public AttendanceDto checkOut(Long companyId, Long employeeId) {
        Attendance attendance = attendanceRepository
                .findByCompanyIdAndEmployeeIdAndDate(companyId, employeeId, LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("No check-in found"));

        attendance.setCheckOutTime(LocalDateTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> getTodayAttendance(Long companyId) {
        return attendanceRepository.findByCompanyIdAndDate(companyId, LocalDate.now())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getTodayPresentCount(Long companyId) {
        return attendanceRepository.countByCompanyIdAndDate(companyId, LocalDate.now());
    }

    private AttendanceDto toDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeCode(a.getEmployee().getEmployeeCode());
        dto.setEmployeeName(a.getEmployee().getFirstName()
                + (a.getEmployee().getLastName() != null ? " " + a.getEmployee().getLastName() : ""));
        dto.setDate(a.getDate());
        dto.setCheckInTime(a.getCheckInTime());
        dto.setCheckOutTime(a.getCheckOutTime());
        dto.setStatus(a.getStatus());
        return dto;
    }
}
