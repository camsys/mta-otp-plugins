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
package com.camsys.otp.plugins.threads;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.plugin.Pluggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractThreadPoolPlugin<T> implements Pluggable, RejectedExecutionHandler {

    private static final Logger _log = LoggerFactory.getLogger(AbstractThreadPoolPlugin.class);

    private ExecutorService executor;

    @Override
    public void init(JsonNode config) {
        int nThreads = 1;
        int nQueueCapacity = Integer.MAX_VALUE;
        if (config != null && config.get("nThreads") != null) {
            nThreads = config.get("nThreads").asInt(1);
        }
        if (config != null && config.get("nQueueCapacity") != null) {
            nQueueCapacity = config.get("nQueueCapacity").asInt(nQueueCapacity);
        }
        executor = new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(nQueueCapacity),
                Executors.defaultThreadFactory(),
                this);
    }

    @Override
    public Object receive(Object obj) {
        if (getSubscription().isInstance(obj)) {
            T message = getSubscription().cast(obj);
            return executor.submit(() -> safeProcess(message));
        }
        return null;
    }

    @Override
    public List<Class<?>> getSubscriptions() {
        return Collections.singletonList(getSubscription());
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        _log.error("Unable to enqueue task {}", r);
    }

    void safeProcess(T message) {
        try {
            process(message);
        } catch (Exception e) {
            _log.error("Error: {}", e);
        }
    }

    public abstract Class<T> getSubscription();

    public abstract void process(T message);
}
