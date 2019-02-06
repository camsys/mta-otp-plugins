package com.camsys.otp.plugins.cloudwatch.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import java.util.HashSet;
import java.util.Set;

public class CountMetrics implements CloudWatchMetrics {

    private Set<MetricDatum> metrics = new HashSet<>();


    public void addCountMetric(final String metricName, final Integer value, final String dimensionName, final String dimensionVal){
        Dimension dim = new Dimension()
                .withName(dimensionName)
                .withValue(dimensionVal);
        MetricDatum datum = new MetricDatum()
                .withMetricName(metricName)
                .withValue((double)value)
                .withUnit(StandardUnit.Count)
                .withDimensions(dim);
        metrics.add(datum);
    }

    public void addPercentMetric(final String metricName, final double value, final String dimensionName, final String dimensionVal){
        Dimension dim = new Dimension()
                .withName(dimensionName)
                .withValue(dimensionVal);
        MetricDatum datum = new MetricDatum()
                .withMetricName(metricName)
                .withValue(value * 100.0)
                .withUnit(StandardUnit.Percent)
                .withDimensions(dim);
        metrics.add(datum);
    }


    @Override
    public Set<MetricDatum> getMetrics() {
        return metrics;
    }

}
