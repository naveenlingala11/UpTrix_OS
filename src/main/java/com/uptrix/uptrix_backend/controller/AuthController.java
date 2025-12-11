package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.AuthResponse;
import com.uptrix.uptrix_backend.dto.LoginRequest;
import com.uptrix.uptrix_backend.dto.RegisterAdminRequest;
import com.uptrix.uptrix_backend.dto.identifier.IdentifierCheckRequest;
import com.uptrix.uptrix_backend.dto.identifier.IdentifierCheckResponse;
import com.uptrix.uptrix_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login user", description = "Authenticate user and return JWT/auth details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register tenant admin", description = "Create a new tenant with an admin user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registered",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register-admin")
    public ResponseEntity<AuthResponse> registerAdmin(@RequestBody RegisterAdminRequest request) {
        AuthResponse response = authService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check identifier availability",
            description = "Validates whether an identifier (subdomain, email or code) is available")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check result",
                    content = @Content(schema = @Schema(implementation = IdentifierCheckResponse.class)))
    })
    @PostMapping("/check-identifier")
    public ResponseEntity<IdentifierCheckResponse> checkIdentifier(
            @RequestBody IdentifierCheckRequest request
    ) {
        IdentifierCheckResponse response = authService.checkIdentifier(request);
        return ResponseEntity.ok(response);
    }
}
