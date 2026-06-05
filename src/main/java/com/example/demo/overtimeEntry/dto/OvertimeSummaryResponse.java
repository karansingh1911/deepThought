package com.example.demo.overtimeEntry.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeSummaryResponse {

    private Long workerId;

    private String workerName;

    private BigDecimal totalOvertimeHours;

    private BigDecimal totalPayoutAmount;

    private String settlementStatus;

    private List<OvertimeDayBreakdownDto> breakdown;
}