package com.example.call_track.controller;

import com.example.call_track.dto.user.PasswordChangeDto;
import com.example.call_track.dto.user.UpdateDto;
import com.example.call_track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("userUpdateDto", new UpdateDto());
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        model.addAttribute("user", userService.getCurrentAuthenticatedUser());
        return "profile";
    }
}