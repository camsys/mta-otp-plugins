package com.camsys.otp.plugins.threads;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.plugin.Pluggable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractThreadPoolPlugin<T> implements Pluggable {

    private ExecutorService executor;

    @Override
    public void init(JsonNode config) {
        int nThreads = 1;
        if (config != null) {
            nThreads = config.get("nThreads").asInt(1);
        }
        executor = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public Object receive(Object obj) {
        if (getSubscription().isInstance(obj)) {
            T message = getSubscription().cast(obj);
            return executor.submit(() -> this.process(message));
        }
        return null;
    }

    @Override
    public List<Class<?>> getSubscriptions() {
        return Collections.singletonList(getSubscription());
    }

    public abstract Class<T> getSubscription();

    public abstract void process(T message);
}
