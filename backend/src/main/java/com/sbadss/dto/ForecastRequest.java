package com.sbadss.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastRequest {

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @Min(value = 1, message = "Forecast horizon must be at least 1 day")
    private int horizonDays = 30;
}
