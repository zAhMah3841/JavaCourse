package com.example.call_track.controller;

import com.example.call_track.dto.call.CallDto;
import com.example.call_track.dto.call.CallPageDto;
import com.example.call_track.dto.user.PublicUserDto;
import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.user.User;
import com.example.call_track.service.CallService;
import com.example.call_track.service.PhoneNumberService;
import com.example.call_track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CallsController extends BaseController {

    private final CallService callService;
    private final UserService userService;
    private final PhoneNumberService phoneNumberService;

    @GetMapping("/calls")
    public String calls(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
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
                .avatarPath(user.getAvatarPath())
                .publicContactInfo(user.getPublicContactInfo())
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/calls")
    public ResponseEntity<CallPageDto> getUserCalls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String myNumbers,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String callType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) java.math.BigDecimal minCost,
            @RequestParam(required = false) java.math.BigDecimal maxCost,
            @RequestParam(required = false) java.math.BigDecimal pricePerMinute,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice
    ) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        // Прокидываем все фильтры дальше
        org.springframework.data.domain.Page<Call> callPage = callService.searchUserCalls(
                currentUser, name, myNumbers, phone,
                callType, startDate, endDate,
                sortBy, sortDir, minCost, maxCost,
                pricePerMinute, minPrice, maxPrice, pageable
        );

        List<CallDto> callDtos = callPage.getContent().stream().map(call -> {
            boolean isOutgoing = call.getCallerPhone().getUser().getId().equals(currentUser.getId());
            PhoneNumber otherPhone = isOutgoing ? call.getCalleePhone() : call.getCallerPhone();
            User otherUser = otherPhone.getUser();
            String otherPartyName = (otherUser.getFirstName() != null ? otherUser.getFirstName() : "")
                    + " " + (otherUser.getLastName() != null ? otherUser.getLastName() : "")
                    + (otherUser.getMiddleName() != null ? " " + otherUser.getMiddleName() : "");
            String userPhone = isOutgoing ? call.getCallerPhone().getPhone() : call.getCalleePhone().getPhone();
            String type = isOutgoing ? "OUTGOING" : "INCOMING";
            String duration = String.format("%02d:%02d", call.getDurationSeconds() / 60, call.getDurationSeconds() % 60);
            return CallDto.builder()
                    .otherPartyName(otherPartyName.trim())
                    .otherPartyPhone(otherPhone.getPhone())
                    .userPhone(userPhone)
                    .type(type)
                    .duration(duration)
                    .callTime(call.getCallDateTime())
                    .tariff(call.getPricePerMinute())
                    .cost(call.getTotalCost())
                    .build();
        }).collect(Collectors.toList());

        CallPageDto response = CallPageDto.builder()
                .calls(callDtos)
                .currentPage(callPage.getNumber())
                .totalPages(callPage.getTotalPages())
                .totalElements(callPage.getTotalElements())
                .pageSize(callPage.getSize())
                .hasNext(callPage.hasNext())
                .hasPrevious(callPage.hasPrevious())
                .build();

        return ResponseEntity.ok(response);
    }
}