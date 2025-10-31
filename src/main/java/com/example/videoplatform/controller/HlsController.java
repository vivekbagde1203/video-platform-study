package com.example.videoplatform.controller;

import com.example.videoplatform.security.JwtService;
import com.example.videoplatform.service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
public class HlsController {

    private final JwtService jwtService;
    private final VideoService videoService;

    public HlsController(JwtService jwtService, VideoService videoService) {
        this.jwtService = jwtService;
        this.videoService = videoService;
    }

    // 🎞 Serve HLS playlist (main manifest)
    @GetMapping(path = "/hls/{videoId}/playlist.m3u8")
    @ResponseBody
    public ResponseEntity<Resource> serveManifest(@PathVariable String videoId,
                                                  @RequestParam(required = false) String token,
                                                  Authentication auth) {
        String username = (auth != null) ? auth.getName() : "guest";
        String path = "/hls/" + videoId + "/playlist.m3u8";

        // ✅ Validate JWT token for students
        if (token == null || !jwtService.validateToken(token, path, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File file = new File("videos/hls/" + videoId + "/playlist.m3u8");
        if (!file.exists()) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    // 🎬 Serve individual .ts video segments
    @GetMapping(path = "/hls/{videoId}/{segment:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveSegment(@PathVariable String videoId,
                                                 @PathVariable String segment,
                                                 @RequestParam(required = false) String token,
                                                 Authentication auth) {
        String username = (auth != null) ? auth.getName() : "guest";
        String path = "/hls/" + videoId + "/" + segment;

        // ✅ Validate JWT token for students
        if (token == null || !jwtService.validateToken(token, path, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File file = new File("videos/hls/" + videoId + "/" + segment);
        if (!file.exists()) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("video/MP2T"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}

