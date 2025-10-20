package com.example.videoplatform.service;

import com.example.videoplatform.model.AppUser;
import com.example.videoplatform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // ✅ Register student with default ROLE_STUDENT
    public AppUser registerStudent(String username, String password) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRole("ROLE_STUDENT");
        return repo.save(user);
    }

    // ✅ Generic method for finding users
    public Optional<AppUser> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    // ✅ List all users for admin dashboard
    public List<AppUser> listAllUsers() {
        return repo.findAll();
    }

    // ✅ Delete user by ID
    public void deleteUser(Long id) {
        repo.deleteById(id);
    }

    // ✅ Admin can create users with any valid role (auto add ROLE_ prefix)
    public AppUser createUser(String username, String password, String role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));

        // ✨ Safe role normalization
        if (role == null || role.trim().isEmpty()) {
            role = "STUDENT"; // default fallback
        }

        if (!role.toUpperCase().startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        user.setRole(role);
        return repo.save(user);
    }

    // ✅ Optional: Ensure at least one admin exists at startup
    public void ensureAdminExists() {
        if (repo.findByUsername("admin").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("adminpass"));
            admin.setRole("ROLE_ADMIN");
            repo.save(admin);
            System.out.println("✅ Default admin created: admin / adminpass");
        }
    }
}
