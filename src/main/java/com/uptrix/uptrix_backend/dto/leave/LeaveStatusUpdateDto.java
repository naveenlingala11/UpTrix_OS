package com.uptrix.uptrix_backend.dto.leave;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveStatusUpdateDto {

    private String status;   // APPROVED / REJECTED / PENDING
    private String comment;  // optional
}
