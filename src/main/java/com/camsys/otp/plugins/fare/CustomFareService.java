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
