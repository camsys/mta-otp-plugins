/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
