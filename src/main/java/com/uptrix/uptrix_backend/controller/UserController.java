package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.user.UserCreateRequest;
import com.uptrix.uptrix_backend.dto.user.UserResponseDto;
import com.uptrix.uptrix_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/users")
@Tag(name = "Users", description = "Admin endpoints to manage application users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create user", description = "Create a new application user for a company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@PathVariable Long companyId,
                                                      @RequestBody UserCreateRequest request) {
        UserResponseDto dto = userService.createUserForCompany(companyId, request);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "List users", description = "List all users for the company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> listUsers(@PathVariable Long companyId) {
        return ResponseEntity.ok(userService.listUsersByCompany(companyId));
    }
}
