package com.simon.application.dto.response;

import com.simon.application.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private DriverStatus driverStatus;

    private LocalDateTime createdAt;
}
