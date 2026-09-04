package com.simon.application.client;

import com.simon.application.exception.ResourceNotFoundException;
import com.simon.application.exception.UpstreamServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DriverServiceClient {

    private final RestClient restClient;

    public DriverServiceClient(RestClient.Builder restClientBuilder,
                                @Value("${driver-service.base-url}") String driverServiceBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(driverServiceBaseUrl).build();
    }

    /**
     * Live check at assignment time - "is this driver free right now" is exactly
     * the kind of decision-gate call that stays synchronous even though the
     * resulting status flip afterward is async. See docs/MICROSERVICES.md's
     * sync-vs-async table and the accepted double-booking race it documents.
     */
    public DriverSummaryResponse getDriverSummary(Long driverId) {

        try {
            return restClient.get()
                    .uri("/internal/drivers/{id}", driverId)
                    .retrieve()
                    .body(DriverSummaryResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Driver not found with id: " + driverId);
        } catch (RestClientException e) {
            throw new UpstreamServiceException("driver-service is unavailable: " + e.getMessage());
        }
    }
}
