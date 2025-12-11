package com.uptrix.uptrix_backend.security;

import com.uptrix.uptrix_backend.dto.UserPrincipal;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.Role;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public JwtAuthFilter(JwtService jwtService,
                         UserRepository userRepository,
                         EmployeeRepository employeeRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtService.extractUserId(token);

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findById(userId).orElse(null);

            if (user != null && user.isActive()) {

                Set<String> roleNames = user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet());

                Long employeeId = employeeRepository.findByUser(user)
                        .map(Employee::getId)
                        .orElse(null);

                UserPrincipal principal = UserPrincipal.of(
                        user.getId(),
                        employeeId,
                        user.getUsername(),
                        user.getPassword(),
                        roleNames
                );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
