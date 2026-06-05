package com.example.demo.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CheckInRequest {
    @NotNull
    private Long workerId;
    @NotNull
    private Long siteId;
}