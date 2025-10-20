package com.example.videoplatform.controller;

import com.example.videoplatform.security.JwtService;
import com.example.videoplatform.service.UserService;
import com.example.videoplatform.service.VideoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final VideoService videoService;
    private final UserService userService;
    private final JwtService jwtService;

    public AdminController(VideoService videoService, UserService userService, JwtService jwtService) {
        this.videoService = videoService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public String adminPage(Model model, @RequestParam(value = "hlsUrl", required = false) String hlsUrl) {
        model.addAttribute("videos", videoService.listAll());
        model.addAttribute("users", userService.listAllUsers());
        if (hlsUrl != null) model.addAttribute("hlsUrl", hlsUrl);
        return "admin";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return "redirect:/admin?error=No+file+selected";
            videoService.store(file);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin?error=Upload+failed";
        }
        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id) {
        try { videoService.delete(id); } catch (Exception e) { e.printStackTrace(); return "redirect:/admin?error=Delete+failed"; }
        return "redirect:/admin";
    }

    @PostMapping("/createUser")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role) {
        try { userService.createUser(username, password, role); } catch (Exception e) { e.printStackTrace(); return "redirect:/admin?error=User+creation+failed"; }
        return "redirect:/admin";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam Long id) {
        try { userService.deleteUser(id); } catch (Exception e) { e.printStackTrace(); return "redirect:/admin?error=Delete+user+failed"; }
        return "redirect:/admin";
    }

    // Generate HLS signed URL (GET)
    @GetMapping("/generateHlsToken")
    public String generateHlsToken(@RequestParam String videoId, Model model) {
        String path = "/hls/" + videoId + "/playlist.m3u8";
        String token = jwtService.createTokenForPath(path);
        String url = "/hls/" + videoId + "/playlist.m3u8?token=" + token;
        model.addAttribute("hlsUrl", url);
        model.addAttribute("videos", videoService.listAll());
        model.addAttribute("users", userService.listAllUsers());
        return "admin";
    }
}
