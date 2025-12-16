package com.example.call_track.controller;

import com.example.call_track.entity.user.User;
import com.example.call_track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            User currentUser = userService.getCurrentAuthenticatedUser();
            model.addAttribute("user", currentUser);
            model.addAttribute("isAdmin", currentUser.getRole().name().equals("ADMIN"));
        }
        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/home")
    public String homePage() {
        return "redirect:/dashboard";
    }
}