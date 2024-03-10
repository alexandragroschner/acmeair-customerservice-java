package com.acmeair.client.fallback;

import com.acmeair.client.responses.CustomerMilesResponse;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import java.util.logging.Logger;

@Dependent
public class BookingFallbackHandler implements FallbackHandler<CustomerMilesResponse> {
    protected static Logger logger =  Logger.getLogger(BookingFallbackHandler.class.getName());

    @Override
    public CustomerMilesResponse handle(ExecutionContext context) {
        System.out.println("Booking Call Failed - check connection to Booking Service.");
        logger.info("fallback for " + context.getMethod().getName());
        return null;
    }
}
