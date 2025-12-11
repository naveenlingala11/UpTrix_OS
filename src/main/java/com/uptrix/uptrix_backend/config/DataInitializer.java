package com.uptrix.uptrix_backend.config;

import com.uptrix.uptrix_backend.constants.RoleNames;
import com.uptrix.uptrix_backend.entity.Role;
import com.uptrix.uptrix_backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // ✅ Uptrix Side
        createRoleIfNotExists(RoleNames.SUPER_ADMIN, "Uptrix platform super administrator");

        // ✅ Client Organization Side
        createRoleIfNotExists(RoleNames.ORG_ADMIN, "Organization admin with full control");
        createRoleIfNotExists(RoleNames.IT_ADMIN, "IT admin for SSO, access & integrations");

        createRoleIfNotExists(RoleNames.CHRO, "Chief HR Officer");
        createRoleIfNotExists(RoleNames.HRBP, "HR Business Partner");
        createRoleIfNotExists(RoleNames.HR_EXEC, "HR Operations Executive");

        createRoleIfNotExists(RoleNames.RECRUITER, "Talent acquisition & hiring");

        createRoleIfNotExists(RoleNames.MANAGER, "Team & people manager");
        createRoleIfNotExists(RoleNames.EMPLOYEE, "Standard employee self-service access");

        createRoleIfNotExists(RoleNames.PAYROLL, "Payroll & finance access");
        createRoleIfNotExists(RoleNames.LND, "Learning & development team");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }
}
