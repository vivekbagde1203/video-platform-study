package com.example.videoplatform.repository;
import com.example.videoplatform.model.VideoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VideoRepository extends JpaRepository<VideoMeta, Long> {}
