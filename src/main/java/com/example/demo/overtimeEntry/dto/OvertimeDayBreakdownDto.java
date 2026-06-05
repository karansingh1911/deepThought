package com.example.demo.overtimeEntry.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeDayBreakdownDto {

    private LocalDate date;
    private BigDecimal overtimeHours;
    private BigDecimal amount;
    private String settlementStatus;
}