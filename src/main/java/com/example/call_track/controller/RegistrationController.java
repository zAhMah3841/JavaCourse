package com.example.call_track.controller;

import com.example.call_track.dto.user.RegistrationDto;
import com.example.call_track.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController extends BaseController {
    private final UserService userService;

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationDto());
        return "register";
    }

    @PostMapping
    public String registerUser(@Valid @ModelAttribute("user") RegistrationDto registrationDto, Model model,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "register";

        try {
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please login with your credentials.");

            return "redirect:/login";
        } catch (Exception e) {
            handleError(model, e, "Registration failed. Please try again.");
            return "register";
        }
    }
}

