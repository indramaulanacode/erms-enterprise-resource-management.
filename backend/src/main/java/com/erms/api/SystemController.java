package com.erms.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {
    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("name", "ERMS API", "version", "0.1.0", "status", "running");
    }
}