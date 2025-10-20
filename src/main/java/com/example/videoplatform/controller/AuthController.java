package com.example.videoplatform.controller;

import com.example.videoplatform.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserService service;
    public AuthController(UserService service){this.service=service;}
    @GetMapping("/signup") public String signupForm(){return "signup";}
    @PostMapping("/signup") public String signup(@RequestParam String username,@RequestParam String password){service.registerStudent(username,password); return "redirect:/login";}
}
