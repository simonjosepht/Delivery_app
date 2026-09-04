package com.simon.application.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * delivery-service's own copy of the response shape from order-service's
 * GET /internal/orders/{id} - not a shared library, see docs/MICROSERVICES.md.
 * Only carries what this service actually uses (existence check); order-service's
 * response also has a "status" field, silently ignored here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private Long id;
}
