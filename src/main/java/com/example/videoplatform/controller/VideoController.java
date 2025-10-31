package com.example.videoplatform.controller;

import com.example.videoplatform.model.VideoMeta;
import com.example.videoplatform.security.JwtService;
import com.example.videoplatform.service.VideoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
public class VideoController {

    private final VideoService service;
    private final JwtService jwtService;

    public VideoController(VideoService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    // 🎞 Role-based homepage
    @GetMapping({"/", "/videos"})
    public String list(Model model, Authentication auth) {
        List<VideoMeta> allVideos = service.listAll();
        model.addAttribute("videos", allVideos);

        if (auth != null) {
            String username = auth.getName();
            model.addAttribute("username", username);

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                model.addAttribute("role", "ROLE_ADMIN");
                return "admin";
            } else {
                model.addAttribute("role", "ROLE_STUDENT");
                return "videos";
            }
        }

        model.addAttribute("username", "Guest");
        model.addAttribute("role", "ROLE_GUEST");
        return "videos";
    }

    // 🎥 Watch Video (Admin full access, Student with signed token)
    @GetMapping("/videos/watch/{filename}")
    public String watch(@PathVariable String filename, Authentication auth, Model model) {
        VideoMeta video = service.findByFilename(filename);
        if (video == null) {
            model.addAttribute("error", "Video not found");
            return "error";
        }

        String username = auth != null ? auth.getName() : "guest";
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("video", video);
        model.addAttribute("username", username);
        model.addAttribute("role", isAdmin ? "ROLE_ADMIN" : "ROLE_STUDENT");

        // ✅ Generate JWT token for student access (expires in minutes)
        if (!isAdmin) {
            String token = jwtService.createTokenForPath("/videos/stream/" + filename, username);
            String signedUrl = "/videos/stream/" + filename + "?token=" + token;
            model.addAttribute("streamUrl", signedUrl);
        } else {
            model.addAttribute("streamUrl", "/videos/stream/" + filename);
        }

        return "watch";
    }

    // 🎬 Stream Endpoint (JWT-secured for Students)
    @GetMapping("/videos/stream/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> stream(@PathVariable String filename,
                                           @RequestParam(required = false) String token,
                                           Authentication auth) {
        String username = (auth != null) ? auth.getName() : "guest";
        String path = "/videos/stream/" + filename;

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 🔒 Validate student tokens
        if (!isAdmin) {
            if (token == null || !jwtService.validateToken(token, path, username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Resource resource = service.getVideoResource(filename);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }

    // 📥 Download (Only Admin or Role_DOWNLOAD)
    @GetMapping("/videos/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename, Authentication auth) throws IOException {
        boolean canDownload = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOWNLOAD") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!canDownload) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Resource resource = service.getVideoResource(filename);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}

