package com.acmeair.client;

import com.acmeair.client.fallback.BookingFallbackHandler;
import com.acmeair.client.responses.CustomerMilesResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.io.IOException;
import java.time.temporal.ChronoUnit;

@RegisterRestClient(configKey = "bookingClient")
@Path("/")
public interface BookingClient {
    @GET
    @Path("/customerreward/{id}")
    @Produces("application/json")
    @Timeout(10000) // throws exception after 500 ms which invokes fallback handler
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, successThreshold = 10, delay = 1, delayUnit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 3, delayUnit = ChronoUnit.SECONDS, delay = 5, durationUnit = ChronoUnit.SECONDS,
            maxDuration = 30, retryOn = Exception.class, abortOn = IOException.class)
    @Fallback(BookingFallbackHandler.class)
    public CustomerMilesResponse getCustomerRewards(@PathParam("id") String customerId);
}
