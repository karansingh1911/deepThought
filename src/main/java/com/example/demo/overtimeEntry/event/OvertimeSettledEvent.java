package com.example.demo.overtimeEntry.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OvertimeSettledEvent {

    private Long workerId;
    private String month;
    private BigDecimal amount;
}