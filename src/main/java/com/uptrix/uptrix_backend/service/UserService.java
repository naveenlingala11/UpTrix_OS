package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.user.UserCreateRequest;
import com.uptrix.uptrix_backend.dto.user.UserResponseDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.Role;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.RoleRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto createUserForCompany(Long companyId, UserCreateRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.getRoleName()));

        if (userRepository.existsByCompanyIdAndEmail(companyId, request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists in the company");
        }

        User user = new User();
        user.setCompany(company);
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(role);

        User saved = userRepository.save(user);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> listUsersByCompany(Long companyId) {
        return userRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private UserResponseDto toDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setActive(user.isActive());
        dto.setPrimaryRole(
                user.getRoles().stream()
                        .findFirst()
                        .map(Role::getName)
                        .orElse(null)
        );
        return dto;
    }
}
