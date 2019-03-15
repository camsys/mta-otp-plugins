package com.camsys.otp.plugins.metrics;

import com.camsys.otp.plugins.threads.AbstractThreadPoolPlugin;
import com.fasterxml.jackson.databind.JsonNode;
import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.opentripplanner.updater.stoptime.TripUpdateStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsReportingService extends AbstractThreadPoolPlugin<TripUpdateStats> {

    private static final Logger _log = LoggerFactory.getLogger(MetricsReportingService.class);

    private boolean _enabled = false;

    private ExternalServices _externalServices = new ExternalServicesBridgeFactory().getExternalServices();

    private String _namespace;

    @Override
    public synchronized void init(JsonNode awsConfig) {
        super.init(awsConfig);
        try {
            _namespace = getConfigValue(awsConfig, "namespace");
            _enabled = true;
        } catch(Exception e){
            _log.warn("Unable to connect to CloudWatch", e);
            _enabled = false;
        }
    }

    @Override
    public Class<TripUpdateStats> getSubscription() {
        return TripUpdateStats.class;
    }

    @Override
    public void process(TripUpdateStats stats) {
        if(enabled()){

            int appliedUpdates = stats.getAppliedUpdates();
            int totalUpdates = stats.getTotalUpdates();
            int scheduledSuccess = stats.getScheduledSuccess();
            int scheduledUpdates = stats.getScheduledUpdates();
            int addedSuccess = stats.getAddedSuccess();
            int addedUpdates = stats.getAddedUpdates();
            int cancelledSuccess = stats.getCancelledSuccess();
            int cancelledUpdates = stats.getCancelledUpdates();
            String feedId = stats.getFeedId();

            publishMetric(feedId, "totalTripUpdatesSuccess", appliedUpdates);
            publishMetric(feedId, "totalTripUpdatesFailed", totalUpdates - appliedUpdates);
            publishMetric(feedId, "totalTripUpdates", totalUpdates);
            publishMetric(feedId, "scheduledSuccess", scheduledSuccess);
            publishMetric(feedId, "scheduledUpdates", scheduledUpdates);
            publishMetric(feedId, "addedSuccess", addedSuccess);
            publishMetric(feedId, "addedSuccess", addedUpdates);
            publishMetric(feedId, "cancelledSuccess", cancelledSuccess);
            publishMetric(feedId, "cancelledUpdates", cancelledUpdates);

            publishMetric(feedId, "vehiclesInServicePct", getMetricPctValues(totalUpdates, appliedUpdates));
            publishMetric(feedId, "scheduledTripsSuccessPct", getMetricPctValues(scheduledUpdates, scheduledSuccess));
            publishMetric(feedId, "addedTripsSuccessPct", getMetricPctValues(addedUpdates, addedSuccess));
            publishMetric(feedId, "cancelledTripsSuccessPct", getMetricPctValues(cancelledUpdates, cancelledSuccess));
        } else {
            _log.info("cloudwatch not enabled, total updates {}", stats.getAppliedUpdates());
        }
    }

    private double getMetricPctValues(int total, int successful){
        if(total < 1)
            return total;
        return ((double)(total - (total-successful))/total) * 100;
    }

    private boolean enabled(){
        return _enabled;
    }

    private void publishMetric(String feedName, String metricName, double value) {
        if (_externalServices.isInstancePrimary()) {
            _externalServices.publishMetric(_namespace, metricName, "feed", feedName, value);
        }
    }
}