package com.example.videoplatform.controller;

import com.example.videoplatform.model.VideoMeta;
import com.example.videoplatform.service.VideoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class VideoWatchController {

    private final VideoService videoService;

    public VideoWatchController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/videos/watch/{filename}")
    public String watchVideo(@PathVariable String filename, Model model) {
        // Fetch video metadata
        VideoMeta video = videoService.listAll()
                .stream()
                .filter(v -> v.getFilename().equals(filename))
                .findFirst()
                .orElse(null);

        if (video == null) {
            model.addAttribute("error", "Video not found");
            return "error";
        }

        model.addAttribute("video", video);
        return "watch";
    }
}

