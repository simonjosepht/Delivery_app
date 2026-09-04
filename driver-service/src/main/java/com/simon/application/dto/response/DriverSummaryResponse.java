package com.simon.application.dto.response;

import com.simon.application.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal internal-only projection, returned by the service-to-service lookup
 * endpoint delivery-service calls at driver-assignment time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverSummaryResponse {

    private Long id;

    private DriverStatus driverStatus;
}
