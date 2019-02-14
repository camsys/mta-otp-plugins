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
package com.camsys.otp.plugins.fare;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.plugin.Pluggable;
import org.opentripplanner.routing.core.Fare;
import org.opentripplanner.routing.core.Money;
import org.opentripplanner.routing.impl.DefaultFareServiceImpl;
import org.opentripplanner.routing.services.FareService;
import org.opentripplanner.routing.spt.GraphPath;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

/**
 * Minimal example of a custom fare service. Assume all fares are $1.
 */
public class CustomFareService implements FareService, Pluggable {
    @Override
    public Fare getCost(GraphPath path) {
        Fare fare = new Fare();
        Money money = DefaultFareServiceImpl.getMoney(Currency.getInstance("USD"), 1);
        fare.addFare(Fare.FareType.regular, money);
        return fare;
    }

    @Override
    public void init(JsonNode config) {

    }

    @Override
    public Object receive(Object message) {
        return null;
    }

    @Override
    public List<Class<?>> getSubscriptions() {
        return Collections.singletonList(FareService.class);
    }
}
