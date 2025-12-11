package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class NightAllowancePreviewResponse {

    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    private Integer year;
    private Integer month; // 1-12

    /**
     * Total number of days in the given month.
     */
    private Integer totalDaysInMonth;

    /**
     * Number of days where employee is eligible for night allowance.
     */
    private Integer eligibleDays;

    /**
     * Sum of all earned night allowance amounts.
     */
    private BigDecimal totalAmount;

    /**
     * If all days use the same per-day amount, this will be set.
     * Otherwise null (because multiple shift types with different allowance).
     */
    private BigDecimal constantPerDayAmount;

    private List<DayBreakdown> days;

    @Getter
    @Setter
    public static class DayBreakdown {
        private LocalDate date;

        private boolean hasShift;
        private String shiftCode;
        private Boolean shiftNightFlag;
        private Boolean shiftAutoAllowance;
        private BigDecimal shiftAllowanceAmount;

        private boolean hasAttendance;
        private String attendanceStatus; // PRESENT / ABSENT / HALF_DAY / etc.

        private boolean eligible;
        private BigDecimal earnedAmount;
    }
}
