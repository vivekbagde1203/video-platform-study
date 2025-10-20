package com.example.videoplatform.controller;

import com.example.videoplatform.model.VideoMeta;
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
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
public class VideoController {
    private final VideoService service;
    public VideoController(VideoService service) { this.service = service; }

    @GetMapping({"/", "/videos"})
    public String list(Model model) {
        List<VideoMeta> all = service.listAll();
        model.addAttribute("videos", all);
        return "videos";
    }

    @GetMapping("/videos/stream/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> stream(@PathVariable String filename) {
        Resource r = service.getVideoResource(filename);
        if (!r.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + r.getFilename())
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(r);
    }

    @GetMapping("/videos/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename, Authentication auth) throws IOException {
        boolean canDownload = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOWNLOAD") || a.getAuthority().equals("ROLE_ADMIN"));
        if (!canDownload) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Resource resource = service.getVideoResource(filename);
        if (!resource.exists()) {
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
