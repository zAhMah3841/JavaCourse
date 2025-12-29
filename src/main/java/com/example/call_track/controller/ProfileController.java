package com.example.call_track.controller;

import com.example.call_track.dto.user.PasswordChangeDto;
import com.example.call_track.dto.user.UpdateDto;
import com.example.call_track.entity.user.UserRole;
import com.example.call_track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    @PostMapping("/profile/delete-account")
    public String deleteAccount(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            var currentUser = userService.getCurrentAuthenticatedUser();
            if (currentUser.getRole() == UserRole.ADMIN) {
                throw new IllegalArgumentException("Admins cannot delete their own account");
            }
            userService.deleteOwnAccount();
            // Logout the user
            new SecurityContextLogoutHandler().logout(request, response, null);
            return "redirect:/login?deleted=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete account: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}