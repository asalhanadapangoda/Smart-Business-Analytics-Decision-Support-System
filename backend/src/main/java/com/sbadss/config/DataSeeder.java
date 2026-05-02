package com.sbadss.config;

import com.sbadss.entity.Branch;
import com.sbadss.entity.Role;
import com.sbadss.entity.User;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.RoleRepository;
import com.sbadss.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDemoData() {
        return args -> {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(createRole("ADMIN", "System administrator")));
            Role managerRole = roleRepository.findByName("MANAGER")
                    .orElseGet(() -> roleRepository.save(createRole("MANAGER", "Branch manager")));
            Role cashierRole = roleRepository.findByName("CASHIER")
                    .orElseGet(() -> roleRepository.save(createRole("CASHIER", "Point of sale operator")));

            Branch mainBranch = branchRepository.findByName("Main Branch")
                    .orElseGet(() -> {
                        Branch branch = new Branch();
                        branch.setName("Main Branch");
                        branch.setLocation("Head Office");
                        branch.setContactNumber("+0000000000");
                        branch.setActive(true);
                        return branchRepository.save(branch);
                    });

            createUserIfMissing("admin", "admin@sbadss.local", "System Admin", "admin123", adminRole, mainBranch);
            createUserIfMissing("manager", "manager@sbadss.local", "Branch Manager", "manager123", managerRole, mainBranch);
            createUserIfMissing("cashier", "cashier@sbadss.local", "Cashier User", "cashier123", cashierRole, mainBranch);

            log.info("Demo seed data ensured (roles, branch, users).");
        };
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return role;
    }

    private void createUserIfMissing(
            String username,
            String email,
            String fullName,
            String rawPassword,
            Role role,
            Branch branch
    ) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setBranch(branch);
        user.setActive(true);
        userRepository.save(user);
    }
}
