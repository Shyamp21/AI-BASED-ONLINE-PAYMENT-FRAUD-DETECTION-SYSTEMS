package com.frauddetection.config;

import com.frauddetection.entity.Role;
import com.frauddetection.entity.User;
import com.frauddetection.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    "admin@fraudguard.ai",
                    passwordEncoder.encode("Admin@123"),
                    Role.ROLE_ADMIN,
                    "Security Administrator"
            );
            userRepository.save(admin);
            logger.info("Created default administrator account: admin / Admin@123");
        }

        if (!userRepository.existsByUsername("analyst")) {
            User analyst = new User(
                    "analyst",
                    "analyst@fraudguard.ai",
                    passwordEncoder.encode("Analyst@123"),
                    Role.ROLE_USER,
                    "Fraud Risk Analyst"
            );
            userRepository.save(analyst);
            logger.info("Created default analyst account: analyst / Analyst@123");
        }
    }
}
