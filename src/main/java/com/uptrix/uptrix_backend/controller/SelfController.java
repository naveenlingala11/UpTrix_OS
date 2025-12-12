package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.AttendanceDto;
import com.uptrix.uptrix_backend.dto.leave.LeaveRequestDto;
import com.uptrix.uptrix_backend.dto.NotificationDto;
import com.uptrix.uptrix_backend.dto.SelfProfileDto;
import com.uptrix.uptrix_backend.service.NotificationService;
import com.uptrix.uptrix_backend.service.SelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/self")
@Transactional(readOnly = true)
@Tag(name = "Self Service", description = "Endpoints for the current user")
public class SelfController {

    private final SelfService selfService;
    private final NotificationService notificationService;

    public SelfController(SelfService selfService,
                          NotificationService notificationService) {
        this.selfService = selfService;
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get my profile", description = "Returns profile for authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned",
                    content = @Content(schema = @Schema(implementation = SelfProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    @GetMapping("/profile")
    public ResponseEntity<SelfProfileDto> profile() {
        return ResponseEntity.ok(selfService.getProfile());
    }

    @Operation(summary = "Get my leaves", description = "List leave requests for the logged-in user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeaveRequestDto.class))))
    })
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequestDto>> myLeaves() {
        return ResponseEntity.ok(selfService.getMyLeaves());
    }

    @Operation(summary = "Get my attendance", description = "Return recent attendance for current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AttendanceDto.class))))
    })
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceDto>> myAttendance(
            @RequestParam(defaultValue = "60") int limit
    ) {
        return ResponseEntity.ok(selfService.getMyAttendanceHistory(limit));
    }

    @Operation(summary = "Get my notifications", description = "List notifications for current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationDto.class))))
    })
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> myNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    @Operation(summary = "Mark notification read", description = "Mark a notification as read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marked as read")
    })
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark all notifications read", description = "Mark all notifications for current user as read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All marked as read")
    })
    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllNotificationsRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get unread notifications count", description = "Returns count of unread notifications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned")
    })
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Long> unreadCount() {
        return ResponseEntity.ok(notificationService.getMyUnreadCount());
    }

}
