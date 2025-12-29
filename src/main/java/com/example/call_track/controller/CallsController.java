package com.example.call_track.controller;

import com.example.call_track.dto.user.PublicUserDto;
import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.user.User;
import com.example.call_track.service.CallService;
import com.example.call_track.service.PhoneNumberService;
import com.example.call_track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CallsController extends BaseController {

    private final CallService callService;
    private final UserService userService;
    private final PhoneNumberService phoneNumberService;

    @GetMapping("/calls")
    public String calls(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Call> calls = callService.findByUser(currentUser);
        model.addAttribute("calls", calls);
        model.addAttribute("user", currentUser);
        return "calls";
    }

    @GetMapping("/api/calls/user-info")
    public ResponseEntity<PublicUserDto> getUserInfo(@RequestParam String phone) {
        Optional<PhoneNumber> phoneNumberOpt = phoneNumberService.findByPhone(phone);
        if (phoneNumberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = phoneNumberOpt.get().getUser();
        if (!user.isEnabled()) {
            return ResponseEntity.notFound().build();
        }
        PublicUserDto dto = PublicUserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .phone(phone)
                .build();
        return ResponseEntity.ok(dto);
    }
}