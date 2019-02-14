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
        return Collections.singletonList(HelloWorldResource.class);
    }

    @Override
    public List<Class<?>> getSubscriptions() {
        return Collections.singletonList(OTPApplication.ApiPlugin.class);
    }
}
