package com.camsys.otp.plugins.reporting;

import com.camsys.otp.plugins.threads.AbstractThreadPoolPlugin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.opentripplanner.api.resource.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class PlanReportingService extends AbstractThreadPoolPlugin<Response> {

    private static Logger _log = LoggerFactory.getLogger(PlanReportingService.class);

    @Override
    public void process(Response response) {
        PlanReport report = new PlanReport();
        String fromPlace = response.requestParameters.get("fromPlace");
        String toPlace = response.requestParameters.get("toPlace");
        String[] fromPlaceTokens = fromPlace.split(",");
        String[] toPlaceTokens = toPlace.split(",");
        if (fromPlaceTokens.length != 2 || toPlaceTokens.length != 2) {
            _log.error("Invalid input: fromPlace={}, toPlace={}", fromPlace, toPlace);
        }
        Double fromLat = Double.parseDouble(fromPlaceTokens[0]);
        Double fromLon = Double.parseDouble(fromPlaceTokens[1]);
        Double toLat = Double.parseDouble(toPlaceTokens[0]);
        Double toLon = Double.parseDouble(toPlaceTokens[1]);
        report.setFromLat(fromLat);
        report.setFromLon(fromLon);
        report.setToLat(toLat);
        report.setToLon(toLon);
        Boolean arriveBy = Boolean.parseBoolean(response.requestParameters.get("arriveBy"));
        report.setArriveBy(arriveBy);
        report.setRequestTime(response.debugOutput.getRequestDate());
        report.setServerTime(new Date(response.debugOutput.getStartedCalculating()));
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        try {
            String reportJson = mapper.writeValueAsString(report);
            _log.info("Plan report: {}", reportJson);
        } catch(JsonProcessingException ex) {
            _log.error("Error processing json: {}", ex);
        }
    }

    @Override
    public Class<Response> getSubscription() {
        return Response.class;
    }
}
