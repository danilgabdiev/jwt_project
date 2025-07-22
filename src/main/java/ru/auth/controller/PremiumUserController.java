package ru.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/premium_user")
public class PremiumUserController {
    @GetMapping
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("You are authorized as premium user");
    }
}
