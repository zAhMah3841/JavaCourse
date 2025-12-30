package com.example.call_track.dto.call;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CallPageDto {
    private List<CallDto> calls;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}