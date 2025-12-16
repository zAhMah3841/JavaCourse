package com.example.call_track.controller;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class BaseController {

    protected void handleError(Model model, Exception e, String defaultMessage) {
        if (e instanceof IllegalArgumentException) {
            model.addAttribute("error", e.getMessage());
        } else {
            model.addAttribute("error", defaultMessage);
        }
    }

    protected void handleError(RedirectAttributes redirectAttributes, Exception e, String defaultMessage) {
        if (e instanceof IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", defaultMessage);
        }
    }
}