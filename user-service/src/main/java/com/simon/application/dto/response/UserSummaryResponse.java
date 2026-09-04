package com.simon.application.dto.response;

import com.simon.application.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal internal-only projection of a user, returned by the service-to-service
 * lookup endpoint. Deliberately excludes anything another service has no business
 * seeing (password, phone number, timestamps) - just enough to validate a reference.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;

    private UserRole role;
}
