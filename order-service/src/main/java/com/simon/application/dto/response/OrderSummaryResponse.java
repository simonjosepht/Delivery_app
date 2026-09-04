package com.simon.application.dto.response;

import com.simon.application.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal internal-only projection, returned by the service-to-service lookup
 * endpoint delivery-service calls at delivery-creation time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private Long id;

    private OrderStatus status;
}
