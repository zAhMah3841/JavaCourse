package com.example.call_track.controller;

import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.user.User;
import com.example.call_track.service.CallService;
import com.example.call_track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CallsController extends BaseController {

    private final CallService callService;
    private final UserService userService;

    @GetMapping("/calls")
    public String calls(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Call> calls = callService.findByUser(currentUser);
        model.addAttribute("calls", calls);
        model.addAttribute("user", currentUser);
        return "calls";
    }
}