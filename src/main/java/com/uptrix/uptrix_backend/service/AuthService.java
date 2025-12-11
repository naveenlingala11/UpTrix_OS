package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.AuthResponse;
import com.uptrix.uptrix_backend.dto.LoginRequest;
import com.uptrix.uptrix_backend.dto.RegisterAdminRequest;
import com.uptrix.uptrix_backend.dto.identifier.IdentifierCheckRequest;
import com.uptrix.uptrix_backend.dto.identifier.IdentifierCheckResponse;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.Role;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.RoleRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import com.uptrix.uptrix_backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;

    public AuthService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getCompanyId() == null) {
            throw new IllegalArgumentException("Company id is required");
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        if (request.getUsernameOrEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Username/email and password are required");
        }

        String identifier = request.getUsernameOrEmail().trim();

        User user = userRepository
                .findByUsernameAndCompanyId(identifier, request.getCompanyId())
                .orElseGet(() ->
                        userRepository.findByEmail(identifier)
                                .filter(u -> u.getCompany().getId().equals(request.getCompanyId()))
                                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"))
                );

        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        Long employeeId = employeeRepository.findByUser(user)
                .map(Employee::getId)
                .orElse(null);

        String token = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setCompanyId(company.getId());
        response.setCompanyName(company.getName());
        response.setPrimaryRole(
                user.getRoles().stream()
                        .findFirst()
                        .map(Role::getName)
                        .orElse("EMPLOYEE")
        );
        response.setEmployeeId(employeeId);

        return response;
    }

    public AuthResponse registerAdmin(RegisterAdminRequest request) {

        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already in use");
        });

        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username already in use");
        });

        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setLegalName(request.getLegalName());
        company.setSubdomain(request.getSubdomain());
        company.setStatus("ACTIVE");
        company = companyRepository.save(company);

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not configured"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setCompany(company);
        user.getRoles().add(adminRole);

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setCompanyId(company.getId());
        response.setCompanyName(company.getName());
        response.setPrimaryRole("ADMIN");
        response.setEmployeeId(null); // admin might not have Employee record

        return response;
    }

    public IdentifierCheckResponse checkIdentifier(IdentifierCheckRequest request) {
        if (request.getCompanyId() == null) {
            throw new IllegalArgumentException("Company id is required");
        }

        if (request.getUsernameOrEmail() == null || request.getUsernameOrEmail().isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        String identifier = request.getUsernameOrEmail().trim();

        User user = userRepository
                .findByUsernameAndCompanyId(identifier, request.getCompanyId())
                .orElseGet(() ->
                        userRepository.findByEmail(identifier)
                                .filter(u -> u.getCompany().getId().equals(request.getCompanyId()))
                                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                );

        IdentifierCheckResponse resp = new IdentifierCheckResponse();
        resp.setUserId(user.getId());
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setUsername(user.getUsername());
        resp.setCompanyId(company.getId());
        resp.setCompanyName(company.getName());
        resp.setActive(user.isActive());
        resp.setPrimaryRole(
                user.getRoles().stream()
                        .findFirst()
                        .map(Role::getName)
                        .orElse(null)
        );

        return resp;
    }
}
