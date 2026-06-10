package com.shadownet.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/index.html";
    }
}
