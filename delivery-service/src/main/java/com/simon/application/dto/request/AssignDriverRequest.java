package com.simon.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDriverRequest {

    @NotNull(message = "Driver id is required")
    private Long driverId;
}
