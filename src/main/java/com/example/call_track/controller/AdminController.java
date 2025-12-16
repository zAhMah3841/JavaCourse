package com.example.call_track.controller;

import com.example.call_track.entity.user.User;
import com.example.call_track.entity.user.UserRole;
import com.example.call_track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController extends BaseController {

    private final UserService userService;

    @GetMapping
    public String adminPanel(Model model) {
        List<User> users = userService.findAll(); // Need to add this method
        model.addAttribute("users", users);
        return "admin";
    }

    @PostMapping("/change-role/{userId}")
    public String changeUserRole(@PathVariable UUID userId,
                                 @RequestParam UserRole newRole,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Prevent admin from demoting themselves
            User currentAdmin = userService.getCurrentAuthenticatedUser();
            if (user.getId().equals(currentAdmin.getId()) && newRole != UserRole.ADMIN) {
                throw new IllegalArgumentException("Cannot change your own admin role");
            }

            user.setRole(newRole);
            userService.save(user); // Need to add this method

            redirectAttributes.addFlashAttribute("successMessage",
                    "Role updated successfully for user: " + user.getUsername());
        } catch (Exception e) {
            handleError(redirectAttributes, e, "Failed to update user role");
        }
        return "redirect:/admin";
    }
}