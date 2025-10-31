package com.example.videoplatform.controller;

import com.example.videoplatform.service.VideoService;
import org.springframework.stereotype.Controller;

/**
 * This controller previously handled the /videos/watch/{filename} route.
 * 
 * ✅ It is now DISABLED because VideoController.java has an improved version
 * with full authentication, authorization, and video metadata loading logic.
 *
 * Keeping this file commented allows future developers to see legacy implementation
 * without causing route conflicts.
 */
@Controller
public class VideoWatchController {

    private final VideoService videoService;

    public VideoWatchController(VideoService videoService) {
        this.videoService = videoService;
    }

    /*
    // ⚠️ Deprecated / Duplicate Route:
    // This was the old implementation of /videos/watch/{filename}.
    // It is commented out to prevent ambiguous mapping conflicts.
    //
    // ✅ The new version in VideoController.java supports:
    //    - Role-based access (Student vs Admin)
    //    - Secure JWT/HLS video streaming
    //    - Proper error handling and "watch.html" integration

    @GetMapping("/videos/watch/{filename}")
    public String watchVideo(@PathVariable String filename, Model model) {
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
    */
}

