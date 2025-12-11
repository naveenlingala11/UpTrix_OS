package com.uptrix.uptrix_backend.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanySettingsDto {

    private Long companyId;
    private String workdayStart;
    private String workdayEnd;
    private String defaultLeaveTypes;
    private String timezoneLabel;
}
