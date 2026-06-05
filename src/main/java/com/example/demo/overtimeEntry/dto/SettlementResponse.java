package com.example.demo.overtimeEntry.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {

    private Long workerId;

    private String month;

    private BigDecimal totalSettledAmount;

    private int entriesSettled;
}