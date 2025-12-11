package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryStructureComponentDto {

    private Long id;
    private String componentCode;
    private String componentName;
    private String componentType;
    private String calculationType;
    private Double amountValue;
    private Double percentOfBasic;
    private Boolean taxable;
    private Integer sequenceNo;
}
