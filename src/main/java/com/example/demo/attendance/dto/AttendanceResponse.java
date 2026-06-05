package com.example.demo.attendance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AttendanceResponse {

    private Long attendanceId;
    private Long workerId;
    private String workerName;

    private Long siteId;
    private String siteName;

    private LocalDate attendanceDate;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    private BigDecimal totalHoursWorked;
    private BigDecimal overtimeHours;
    private Boolean flagged;
}