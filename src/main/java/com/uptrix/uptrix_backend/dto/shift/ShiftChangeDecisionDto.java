package com.uptrix.uptrix_backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftChangeDecisionDto {

    /**
     * APPROVE or REJECT
     */
    private String action;

    /**
     * User ID of approver (can be HR/Manager user id).
     */
    private Long approverUserId;

    private String approverRemarks;
}
