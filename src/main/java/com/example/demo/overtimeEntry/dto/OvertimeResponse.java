package com.example.demo.overtimeEntry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OvertimeResponse {

    private Long workerId;
    private String workerName;

    private Double totalOvertimeHours;
}
