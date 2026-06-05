package com.example.demo.worker.dto;

import com.example.demo.common.enums.Designation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class UpdateWorkerRequest {

    @NotBlank
    private String name;

    @NotNull
    private Designation designation;

    @NotNull
    @DecimalMin(value = "0.01") // prevent zero or null values
    private BigDecimal dailyWageRate;
}