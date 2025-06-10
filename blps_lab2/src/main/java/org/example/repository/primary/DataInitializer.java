package org.example.repository.primary;

import org.example.entity.primary.User;
import org.example.entity.primary.Role;
import org.example.entity.enums_status.UserRole;
import org.example.repository.primary.RoleRepository;
import org.example.repository.primary.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        Role userRole = roleRepository.findByRoleName(UserRole.USER)
                .orElseGet(() -> roleRepository.save(new Role(null, UserRole.USER)));

        Role hostRole = roleRepository.findByRoleName(UserRole.HOST)
                .orElseGet(() -> roleRepository.save(new Role(null, UserRole.HOST)));

        userRepository.findByName("user1").orElseGet(() -> {
            User user = new User(null, "user1", passwordEncoder.encode("password123"), new HashSet<>());
            user.getRoles().add(userRole);
            return userRepository.save(user);
        });

        userRepository.findByName("host1").orElseGet(() -> {
            User host = new User(null, "host1", passwordEncoder.encode("hostpassword123"), new HashSet<>());
            host.getRoles().add(hostRole);
            return userRepository.save(host);
        });
    }
}
