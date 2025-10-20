package com.example.videoplatform;

import com.example.videoplatform.model.AppUser;
import com.example.videoplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class VideoPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoPlatformApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("adminpass"));
                admin.setRole("ROLE_ADMIN");
                repo.save(admin);
                System.out.println("Default admin created: admin / adminpass");
            }
        };
    }
}
