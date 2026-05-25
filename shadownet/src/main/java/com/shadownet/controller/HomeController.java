package com.shadownet.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "ShadowNet IDS Backend Running Successfully!";
}
}
