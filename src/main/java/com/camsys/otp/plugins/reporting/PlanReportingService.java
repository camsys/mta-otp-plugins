package com.camsys.otp.plugins.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.api.resource.Response;
import org.opentripplanner.plugin.Pluggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class PlanReportingService implements Pluggable {

    private static Logger _log = LoggerFactory.getLogger(PlanReportingService.class);

    @Override
    public void init(JsonNode config) {

    }

    @Override
    public void receive(Object message) {
        if (message instanceof Response) {
            Response response = (Response) message;
            int nItineraries = response.getPlan() == null ? 0 : response.getPlan().itinerary.size();
            _log.info("Received trip plan with {} itineraries.", nItineraries);
        }
    }

    @Override
    public List<Class<?>> getSubscriptions() {
        return Collections.singletonList(Response.class);
    }
}
