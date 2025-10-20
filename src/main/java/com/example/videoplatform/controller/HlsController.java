package com.example.videoplatform.controller;

import com.example.videoplatform.security.JwtService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
public class HlsController {

    private final JwtService jwtService;

    public HlsController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping(path = "/hls/{videoId}/playlist.m3u8")
    @ResponseBody
    public ResponseEntity<Resource> manifest(@PathVariable String videoId, @RequestParam String token) {
        try {
            var claims = jwtService.parseToken(token);
            String path = claims.getBody().get("path", String.class);
            if (!path.equals("/hls/" + videoId + "/playlist.m3u8")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File f = new File("videos/hls/" + videoId + "/playlist.m3u8");
        if (!f.exists()) return ResponseEntity.notFound().build();
        Resource r = new FileSystemResource(f);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"));
        return ResponseEntity.ok().headers(headers).body(r);
    }

    @GetMapping(path = "/hls/{videoId}/{segment:.+}")
    @ResponseBody
    public ResponseEntity<Resource> segment(@PathVariable String videoId, @PathVariable String segment, @RequestParam String token) {
        try {
            var claims = jwtService.parseToken(token);
            String path = claims.getBody().get("path", String.class);
            if (!path.equals("/hls/" + videoId + "/playlist.m3u8")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File f = new File("videos/hls/" + videoId + "/" + segment);
        if (!f.exists()) return ResponseEntity.notFound().build();
        Resource r = new FileSystemResource(f);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("video/MP2T"));
        return ResponseEntity.ok().headers(headers).body(r);
    }
}
