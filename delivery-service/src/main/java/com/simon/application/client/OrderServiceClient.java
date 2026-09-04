package com.simon.application.client;

import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.exception.UpstreamServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(RestClient.Builder restClientBuilder,
                               @Value("${order-service.base-url}") String orderServiceBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(orderServiceBaseUrl).build();
    }

    /**
     * Validates that an order exists by calling order-service's internal endpoint
     * synchronously - a live gate before creating a delivery against it.
     */
    public void assertOrderExists(Long orderId) {

        try {
            restClient.get()
                    .uri("/internal/orders/{id}", orderId)
                    .retrieve()
                    .body(OrderSummaryResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        } catch (RestClientException e) {
            throw new UpstreamServiceException("order-service is unavailable: " + e.getMessage());
        }
    }
}
