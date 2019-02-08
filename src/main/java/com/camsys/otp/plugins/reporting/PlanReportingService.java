package com.camsys.otp.plugins.reporting;

import com.camsys.otp.plugins.threads.AbstractThreadPoolPlugin;
import org.opentripplanner.api.resource.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanReportingService extends AbstractThreadPoolPlugin<Response> {

    private static Logger _log = LoggerFactory.getLogger(PlanReportingService.class);

    @Override
    public void process(Response response) {
        int nItineraries = response.getPlan() == null ? 0 : response.getPlan().itinerary.size();
        _log.info("Received trip plan with {} itineraries.", nItineraries);
    }

    @Override
    public Class<Response> getSubscription() {
        return Response.class;
    }
}
