package com.uptrix.uptrix_backend.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HrJdResponseDto {

    private String jobDescription;

    public HrJdResponseDto() {
    }

    public HrJdResponseDto(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
