package com.example.call_track.controller;

import com.example.call_track.entity.user.User;
import com.example.call_track.entity.user.UserRole;
import com.example.call_track.service.FakeDataService;
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
    private final FakeDataService fakeDataService;

    @GetMapping
    public String adminPanel(Model model) {
        List<User> users = userService.findAllActive();
        User currentUser = userService.getCurrentAuthenticatedUser();
        model.addAttribute("users", users);
        model.addAttribute("user", currentUser);
        return "admin";
    }

    @PostMapping("/change-role/{userId}")
    public String changeUserRole(@PathVariable UUID userId,
                                 @RequestParam UserRole newRole,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByIdActive(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Prevent admin from changing their own role
            User currentAdmin = userService.getCurrentAuthenticatedUser();
            if (user.getId().equals(currentAdmin.getId())) {
                throw new IllegalArgumentException("Cannot change your own role");
            }

            user.setRole(newRole);
            userService.save(user);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Role updated successfully for user: " + user.getUsername());
        } catch (Exception e) {
            handleError(redirectAttributes, e, "Failed to update user role");
        }
        return "redirect:/admin";
    }

    @PostMapping("/generate-fake-data")
    public String generateFakeData(RedirectAttributes redirectAttributes) {
        try {
            fakeDataService.generateFakeData();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Fake data generated successfully. Check the credentials file for login details.");
        } catch (Exception e) {
            handleError(redirectAttributes, e, "Failed to generate fake data");
        }
        return "redirect:/admin";
    }

    @PostMapping("/delete-user/{userId}")
    public String deleteUser(@PathVariable UUID userId, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByIdActive(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Prevent admin from deleting themselves
            User currentAdmin = userService.getCurrentAuthenticatedUser();
            if (user.getId().equals(currentAdmin.getId())) {
                throw new IllegalArgumentException("Cannot delete your own account");
            }

            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            handleError(redirectAttributes, e, "Failed to delete user");
        }
        return "redirect:/admin";
    }
}