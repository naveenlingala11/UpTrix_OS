package com.uptrix.uptrix_backend.dto.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HrJdRequestDto {

    private Long companyId;
    private Long departmentId;

    private String jobTitle;
    private String seniorityLevel;   // e.g. JUNIOR, MID, SENIOR, LEAD
    private String employmentType;   // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    private String location;
    private String workMode;         // REMOTE, HYBRID, ONSITE

    private List<String> mustHaveSkills;
    private List<String> niceToHaveSkills;

    private String summaryNotes;     // extra notes from HR
    private String tone;             // FORMAL, FRIENDLY, NEUTRAL, SALESY, etc.
}
