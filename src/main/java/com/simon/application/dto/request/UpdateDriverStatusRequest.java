package com.simon.application.dto.request;

import com.simon.application.enums.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDriverStatusRequest {

    @NotNull(message = "Driver status is required")
    private DriverStatus status;
}
