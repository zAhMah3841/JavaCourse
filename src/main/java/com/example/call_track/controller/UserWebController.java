package com.example.call_track.controller;

import com.example.call_track.dto.user.PasswordChangeDto;
import com.example.call_track.dto.user.UpdateDto;
import com.example.call_track.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserWebController extends BaseController {
    private final UserService userService;

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto passwordChangeDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.passwordChangeDto", bindingResult);
            redirectAttributes.addFlashAttribute("passwordChangeDto", passwordChangeDto);

            return "redirect:/profile";
        }

        try {
            userService.changePassword(passwordChangeDto);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Password changed successfully!");
        } catch (Exception e) {
            handleError(redirectAttributes, e, "Password change failed. Please try again.");
            redirectAttributes.addFlashAttribute("passwordChangeDto", passwordChangeDto);
        }

        return "redirect:/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userUpdateDto") UpdateDto updateDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.userUpdateDto", bindingResult);
            redirectAttributes.addFlashAttribute("userUpdateDto", updateDto);

            return "redirect:/profile";
        }

        try {
            userService.updateUserProfile(updateDto);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            handleError(redirectAttributes, e, "Profile update failed. Please try again.");
            redirectAttributes.addFlashAttribute("userUpdateDto", updateDto);
        }

        return "redirect:/profile";
    }

    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            userService.updateAvatar(file);
            redirectAttributes.addFlashAttribute("successMessage", "Avatar updated successfully!");
        } catch (Exception e) {
            handleError(redirectAttributes, e, "Avatar upload failed. Please try again.");
        }
        return "redirect:/profile";
    }
}