package com.camsys.otp.plugins.reporting;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.camsys.otp.plugins.threads.AbstractThreadPoolPlugin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.opentripplanner.api.resource.Response;
import org.opentripplanner.common.model.GenericLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class SQSPlanReportingService extends AbstractThreadPoolPlugin<Response> {

    private static Logger _log = LoggerFactory.getLogger(SQSPlanReportingService.class);

    private AmazonSQS _sqs;

    private String _queueUrl;

    // threadsafe and recommended, see: https://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field
    private ObjectWriter _writer;

    @Override
    public void init(JsonNode config) {
        super.init(config);
        if (config == null) {
            return;
        }
        String accessKey = getConfigValue(config, "accessKey");
        String secretKey = getConfigValue(config, "secretKey");
        _queueUrl = getConfigValue(config, "queueUrl");
        //_log.info("accesskey is {} secret key is {}", accessKey, secretKey);
        if (accessKey == null || secretKey == null || _queueUrl == null) {
            return;
        }
        BasicAWSCredentials cred = new BasicAWSCredentials(accessKey, secretKey);
        _sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(cred))
                .withRegion(Regions.US_EAST_1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        _writer = mapper.writer();
    }

    @Override
    public void process(Response response) {
        if (!enabled()) {
            _log.info("Reporting service not enabled.");
            return;
        }
        PlanReport report = getReport(response);
        String body = reportToJson(report);
        if (body != null) {
            sendMessage(body);
            _log.debug("Sent message {}", body);
        }
    }

    @Override
    public Class<Response> getSubscription() {
        return Response.class;
    }

    private boolean enabled() {
        return _queueUrl != null && _sqs != null;
    }

    private PlanReport getReport(Response response) {
        PlanReport report = new PlanReport();
        String fromPlace = response.requestParameters.get("fromPlace");
        String toPlace = response.requestParameters.get("toPlace");
        GenericLocation from = GenericLocation.fromOldStyleString(fromPlace);
        GenericLocation to = GenericLocation.fromOldStyleString(toPlace);
        if (from.hasCoordinate()) {
            report.setFromLat(from.lat);
            report.setFromLon(from.lng);
        }
        if (to.hasCoordinate()) {
            report.setToLat(to.lat);
            report.setToLon(to.lng);
        }
        if (from.hasPlace()) {
            report.setFromPlace(from.place);
        }
        if (to.hasPlace()) {
            report.setToPlace(to.place);
        }
        Boolean arriveBy = Boolean.parseBoolean(response.requestParameters.get("arriveBy"));
        report.setArriveBy(arriveBy);
        report.setRequestTime(response.debugOutput.getRequestDate());
        report.setServerTime(new Date(response.debugOutput.getStartedCalculating()));
        return report;
    }

    private String reportToJson(PlanReport report) {
        try {
            return _writer.writeValueAsString(report);
        } catch(JsonProcessingException ex) {
            _log.error("Error processing json: {}", ex);
            return null;
        }
    }

    private void sendMessage(String message) {
        _sqs.sendMessage(_queueUrl, message);
    }
}
