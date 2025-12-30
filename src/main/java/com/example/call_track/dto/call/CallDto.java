package com.example.call_track.dto.call;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CallDto {
    private String otherPartyName;
    private String otherPartyPhone;
    private String userPhone;
    private String type;
    private String duration;
    private LocalDateTime callTime;
    private BigDecimal tariff;
    private BigDecimal cost;
}