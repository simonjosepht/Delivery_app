package com.simon.application.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * order-service's own copy of the response shape from user-service's
 * GET /internal/users/{id} - not a shared library, see docs/MICROSERVICES.md.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;

    private String role;
}
