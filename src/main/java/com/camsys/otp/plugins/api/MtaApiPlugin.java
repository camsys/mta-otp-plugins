package com.camsys.otp.plugins.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.plugin.Pluggable;
import org.opentripplanner.standalone.OTPApplication;

import java.util.Collections;
import java.util.List;

public class MtaApiPlugin implements Pluggable, OTPApplication.ApiPlugin {
    @Override
    public void init(JsonNode config) {
    }

    public Object receive(Object message) {
        return null;
    }

    @Override
    public List<Class<?>> getClasses() {
        return Collections.singletonList(RoutePatternsResource.class);
    }

    @Override
    public List<Class<?>> getSubscriptions() {
        return Collections.singletonList(OTPApplication.ApiPlugin.class);
    }
}
