package com.example.videoplatform.service;

import com.example.videoplatform.model.VideoMeta;
import com.example.videoplatform.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {
    private final VideoRepository repo;
    private final Path storagePath;

    public VideoService(VideoRepository repo, @Value("${app.storage.path}") String storage) throws IOException {
        this.repo = repo;
        this.storagePath = Paths.get(storage);
        if (!Files.exists(storagePath)) Files.createDirectories(storagePath);
    }

    public VideoMeta store(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }

        // Generate a unique stored name
        String storedName = UUID.randomUUID().toString() + ext;
        Path target = storagePath.resolve(storedName);
        Files.copy(file.getInputStream(), target);

        // Prepare metadata
        VideoMeta meta = new VideoMeta();
        meta.setFilename(storedName);
        meta.setOriginalName(original);
        meta.setContentType(file.getContentType());
        meta.setSize(file.getSize());
        meta.setUploadedAt(LocalDateTime.now());

        // ✅ Define videoId only once
        String id = storedName.replaceAll("\\.[^.]+$", "");
        meta.setVideoId(id);

        // Save metadata
        meta = repo.save(meta);

        // After saving, create HLS in videos/hls/{id}
        Path hlsDir = storagePath.resolve("hls").resolve(id);
        if (!Files.exists(hlsDir)) Files.createDirectories(hlsDir);

        // Build ffmpeg command
        File inputFile = target.toFile();
        File outPlaylist = hlsDir.resolve("playlist.m3u8").toFile();

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", inputFile.getAbsolutePath(),
                "-profile:v", "baseline", "-level", "3.0", "-start_number", "0",
                "-hls_time", "6", "-hls_list_size", "0", "-f", "hls",
                outPlaylist.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            // wait for ffmpeg to finish for up to 2 minutes
            boolean finished = p.waitFor(120, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
            }
        } catch (Exception e) {
            // log to console; do not fail upload if ffmpeg isn't available
            System.err.println("HLS generation failed: " + e.getMessage());
        }

        return meta;
    }

    public List<VideoMeta> listAll() {
        return repo.findAll();
    }

    public Resource getVideoResource(String filename) {
        return new FileSystemResource(storagePath.resolve(filename).toFile());
    }

    public Path getVideoPath(String filename) {
        return storagePath.resolve(filename);
    }

    public void delete(Long id) throws IOException {
        VideoMeta m = repo.findById(id).orElseThrow();
        Files.deleteIfExists(storagePath.resolve(m.getFilename()));

        // delete hls folder if exists
        String idName = m.getFilename().replaceAll("\\.[^.]+$", "");
        Path hlsDir = storagePath.resolve("hls").resolve(idName);
        if (Files.exists(hlsDir)) {
            Files.walk(hlsDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        repo.deleteById(id);
    }
}

