package com.simon.application.client;

import com.simon.application.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * delivery-service's own copy of the response shape from driver-service's
 * GET /internal/drivers/{id} - not a shared library, see docs/MICROSERVICES.md.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverSummaryResponse {

    private Long id;

    private DriverStatus driverStatus;
}
