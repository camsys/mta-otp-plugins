package com.camsys.otp.plugins.cloudwatch.metrics;

import com.amazonaws.services.cloudwatch.model.MetricDatum;

import java.util.Set;

public interface CloudWatchMetrics {
    Set<MetricDatum> getMetrics();
}
