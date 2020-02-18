/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cornerstonews.webservice;

import java.lang.management.ManagementFactory;

import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.JvmAttributeGaugeSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

public class MetricsRegistryFeature {
    
    private MetricRegistry metricRegistry;
    
    private JmxReporter jmxReporter;
    
    public MetricsRegistryFeature() {
        this.metricRegistry = new MetricRegistry();

        this.metricRegistry.register("jvm.attribute", new JvmAttributeGaugeSet());
        this.metricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        this.metricRegistry.register("jvm.classloader", new ClassLoadingGaugeSet());
        this.metricRegistry.register("jvm.filedescriptor", new FileDescriptorRatioGauge());
        this.metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        this.metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        this.metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());
        jmxReporter = JmxReporter.forRegistry(this.metricRegistry).build();
        jmxReporter.start();
    }

    protected void deregisterMetrics() {
        if (jmxReporter != null) {
            jmxReporter.close();
        }

        for (String metric : this.metricRegistry.getNames()) {
            this.metricRegistry.remove(metric);
        }
    }

    public MetricRegistry getMetricRegistry() {
        return this.metricRegistry;
    }
}
