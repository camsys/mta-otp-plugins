package com.camsys.otp.plugins.cloudwatch;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.util.EC2MetadataUtils;
import com.camsys.otp.plugins.cloudwatch.metrics.CountMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.plugin.Pluggable;
import com.camsys.otp.plugins.cloudwatch.metrics.CloudWatchMetrics;
import org.opentripplanner.updater.stoptime.TripUpdateStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CloudWatchService implements Pluggable {

    private static final Logger _log = LoggerFactory.getLogger(CloudWatchService.class);

    private static volatile CloudWatchService instance;

    private boolean _enabled = false;

    private boolean _primary = false;

    private ScheduledExecutorService _scheduledExecutorService = null;

    private JsonNode _awsConfig = null;

    private String _env;

    private String _accessKey;

    private String _secretKey;

    private AmazonCloudWatchAsync _client;

    private AsyncHandler<PutMetricDataRequest, PutMetricDataResult> _handler;

    @Override
    public synchronized void init(JsonNode awsConfig) {
        try {
            _awsConfig = awsConfig;
            _accessKey = getConfigValue("accessKey");
            _secretKey = getConfigValue("secretKey");
            _env = getConfigValue("environment");

            BasicAWSCredentials cred = new BasicAWSCredentials(_accessKey, _secretKey);
            _client = AmazonCloudWatchAsyncClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(cred)).build();
            _handler = new AsyncHandler<PutMetricDataRequest, PutMetricDataResult>() {
                @Override
                public void onError(Exception e) {
                    _log.error("Error sending to cloudwatch", e);
                }

                @Override
                public void onSuccess(PutMetricDataRequest request, PutMetricDataResult putMetricDataResult) {
                    // do nothing
                }
            };
            _enabled = true;
            scheduleLeadershipElection();
        } catch(Exception e){
            _log.warn("Unable to connect to CloudWatch", e);
            _enabled = false;
        }
    }

    @Override
    public void receive(Object message) {
        if (message instanceof TripUpdateStats) {
            publishStats((TripUpdateStats) message);
        }
    }

    @Override
    public List<Class<?>> getSubscriptions() {
        return Collections.singletonList(TripUpdateStats.class);
    }

    public void publishStats(TripUpdateStats stats) {
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

            CountMetrics countMetrics = new CountMetrics();

            countMetrics.addCountMetric("totalTripUpdatesSuccess", appliedUpdates, "Feed Id", feedId);
            countMetrics.addCountMetric("totalTripUpdatesFailed", totalUpdates - appliedUpdates, "Feed Id", feedId);
            countMetrics.addCountMetric("totalTripUpdates", totalUpdates, "Feed Id", feedId);
            countMetrics.addCountMetric("scheduledSuccess", scheduledSuccess, "Feed Id", feedId);
            countMetrics.addCountMetric("scheduledUpdates", scheduledUpdates, "Feed Id", feedId);
            countMetrics.addCountMetric("addedSuccess", addedSuccess, "Feed Id", feedId);
            countMetrics.addCountMetric("addedUpdates", addedUpdates, "Feed Id", feedId);
            countMetrics.addCountMetric("cancelledSuccess", cancelledSuccess, "Feed Id", feedId);
            countMetrics.addCountMetric("cancelledUpdates", cancelledUpdates, "Feed Id", feedId);

            countMetrics.addPercentMetric("vehiclesInServicePct", getMetricPctValues(totalUpdates, appliedUpdates), "Feed Id", feedId);
            countMetrics.addPercentMetric("scheduledTripsSuccessPct", getMetricPctValues(scheduledUpdates, scheduledSuccess), "Feed Id", feedId );
            countMetrics.addPercentMetric("addedTripsSuccessPct", getMetricPctValues(addedUpdates, addedSuccess), "Feed Id", feedId);
            countMetrics.addPercentMetric("cancelledTripsSuccessPct", getMetricPctValues(cancelledUpdates, cancelledSuccess), "Feed Id", feedId);

            publishMetric("OpenTripPlanner", countMetrics);
        } else {
            _log.info("cloudwatch not enabled, total updates {}", stats.getAppliedUpdates());
        }
    }

    private double getMetricPctValues(int total, int successful){
        if(total < 1)
            return total;
        return (double)(total - (total-successful))/total;
    }

    private String getConfigValue(String key) throws Exception{
        JsonNode value = _awsConfig.get(key);
        return value.asText(null);
    }

    private void scheduleLeadershipElection(){
        try {
            if(_env.equalsIgnoreCase("local")){
                _primary = true;
            }
            else if(EC2MetadataUtils.getInstanceInfo() != null) {
                if(_scheduledExecutorService != null) {
                    _scheduledExecutorService.shutdownNow();
                    Thread.sleep(1 * 1000);
                }
                _scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                _scheduledExecutorService.scheduleAtFixedRate(new LeadershipElectionTask(), 1, 1, TimeUnit.DAYS.MINUTES);
            } else{
                throw new Exception("Unable to get Metadata for AWS Instance");
            }
        } catch (Exception e){
            _log.warn("Unable to connect to AWS Instance.", e);
            _primary = false;
        }
    }

    public boolean enabled(){
        return _enabled && _primary;
    }

    public void publishMetric(String nameSpace, CloudWatchMetrics cwMetrics) {
        if (!enabled()){
            return;
        }
        _client.putMetricDataAsync(new PutMetricDataRequest()
                .withMetricData(cwMetrics.getMetrics())
                .withNamespace(getNameSpaceWithEnv(nameSpace)), _handler);
    }

    private String getNameSpaceWithEnv(String nameSpace){
        if(_env != null && !"".equals(_env)) {
            return _env + ":" + nameSpace;
        }
        return nameSpace;
    }

    public static CloudWatchService getInstance(){
        if(instance == null){
            synchronized (CloudWatchService.class) {
                if(instance == null){
                    instance = new CloudWatchService();
                }
            }
        }
        return instance;
    }

    @PreDestroy
    public void destroy(){
        _scheduledExecutorService.shutdownNow();
    }

    private class LeadershipElectionTask implements Runnable {
        private AmazonAutoScaling _autoScale;
        private AmazonEC2 _ec2;
        private String _autoScalingGroupName;


        public LeadershipElectionTask(){
            try {
                BasicAWSCredentials cred = new BasicAWSCredentials(_accessKey, _secretKey);
                _ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(cred)).build();
                _autoScale = AmazonAutoScalingClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(cred)).build();;
                _autoScalingGroupName = getConfigValue("autoScaleGroup");
            } catch(Exception e){
                _log.warn("Unable to create AWS Clients", e);
                _enabled = false;
                _scheduledExecutorService.shutdownNow();
            }
        }

        public void run() {
            AutoScalingGroup autoScalingGroup = getAutoScalingGroups();

            if(autoScalingGroup != null) {

                String oldestInstance = null;
                Date oldestInstanceLaunchTime = new Date();

                List<String> instanceIds = autoScalingGroup.getInstances().stream()
                        .map(com.amazonaws.services.autoscaling.model.Instance::getInstanceId)
                        .collect(Collectors.toList());

                List<Instance> instances = getInstances(instanceIds);

                for (Instance instance : instances) {
                    if (instance.getLaunchTime().before(oldestInstanceLaunchTime)) {
                        oldestInstanceLaunchTime = instance.getLaunchTime();
                        oldestInstance = instance.getInstanceId();
                    }
                }

                if (oldestInstance != null && oldestInstance.equals(EC2MetadataUtils.getInstanceId())) {
                    _log.warn("This is the primary instance.");
                    _primary = true;
                } else {
                    _log.warn("This is not the primary instance. Oldest Instance Id is {}, this Instance Id is {}", oldestInstance, EC2MetadataUtils.getInstanceId());
                    _primary = false;
                }
            } else {
                _log.warn("Not the primary instance, no autoScaling group found.");
                _primary = false;
            }
        }

        private AutoScalingGroup getAutoScalingGroups(){
            DescribeAutoScalingGroupsResult result = _autoScale.describeAutoScalingGroups(
                    new DescribeAutoScalingGroupsRequest());
            return result.getAutoScalingGroups().stream()
                    .filter(group -> group.getAutoScalingGroupName().startsWith(_autoScalingGroupName))
                    .findFirst().orElse(null);
        }

        private List<Instance> getInstances(List<String> instanceIds){
            try {
                DescribeInstancesRequest request = new DescribeInstancesRequest();
                request.setInstanceIds(instanceIds);
                DescribeInstancesResult result = _ec2.describeInstances(request);
                List<Instance> instances = new ArrayList();
                for (Reservation reservation : result.getReservations()) {
                    instances.addAll(reservation.getInstances());
                }
                return instances;
            } catch (Exception e){
                _log.error("Unable to retreive instances", e);
                return Collections.EMPTY_LIST;
            }
        }
    }

}