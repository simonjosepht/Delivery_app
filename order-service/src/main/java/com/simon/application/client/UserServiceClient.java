package com.simon.application.client;

import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.exception.UpstreamServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder restClientBuilder,
                              @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(userServiceBaseUrl).build();
    }

    /**
     * Validates that a customer exists by calling user-service's internal endpoint
     * synchronously - a live gate before accepting an order, not a cached/replicated
     * read. See docs/MICROSERVICES.md "Which cross-service calls are synchronous".
     */
    public void assertCustomerExists(Long customerId) {

        try {
            restClient.get()
                    .uri("/internal/users/{id}", customerId)
                    .retrieve()
                    .body(UserSummaryResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        } catch (RestClientException e) {
            throw new UpstreamServiceException("user-service is unavailable: " + e.getMessage());
        }
    }
}
