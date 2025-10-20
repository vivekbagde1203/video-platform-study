package com.example.videoplatform.repository;
import com.example.videoplatform.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<AppUser, Long> { Optional<AppUser> findByUsername(String username); }
