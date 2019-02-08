package com.camsys.otp.plugins.api;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

import org.opentripplanner.index.model.PatternDetail;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.ServiceDay;
import org.opentripplanner.routing.edgetype.Timetable;
import org.opentripplanner.routing.edgetype.TripPattern;
import org.opentripplanner.routing.graph.GraphIndex;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *  Return patterns for route
 *
 *  Conceptually this is similar to IndexAPI.getPatternsForRoute, but includes more details and filtering options
 */
@Path("/routers/{routerId}/patternsForRoute")
@XmlRootElement
public class RoutePatternsResource {

    /**
     * Route to return patterns for
     */
    @QueryParam("route")
    private String routeStr;

    /**
     * Epoch time in seconds during which the user is expecting the pattern. defaults to current time.
     * If returnAllPatterns=true, this parameter is ignored.
     */
    @QueryParam("time")
    private Long time;

    /**
     * Return patterns in service from time to time + lookoutSec. Defaults to half an hour.
     */
    @QueryParam("lookoutSec")
    @DefaultValue("1800")
    private int lookoutSec;

    /**
     * If true, return all patterns for route, otherwise, return only patterns
     * that are currently in service;
     */
    @QueryParam("returnAllPatterns")
    private boolean returnAllPatterns = false;

    private GraphIndex index;

    public RoutePatternsResource(@Context OTPServer otpServer, @PathParam("routerId") String routerId) {
        Router router = otpServer.getRouter(routerId);
        index = router.graph.index;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PatternDetail> getPatternsInService() {
        List<PatternDetail> patterns = new ArrayList<>();

        if (routeStr == null)
            throw new IllegalArgumentException("Must provide route.");

        AgencyAndId routeId = AgencyAndId.convertFromString(routeStr, ':');
        Route route = index.routeForId.get(routeId);

        if (returnAllPatterns) {
            if (route != null) {
                Collection<TripPattern> tripPatterns = index.patternsForRoute.get(route);
                for (TripPattern pattern : tripPatterns) {
                    patterns.add(new PatternDetail(pattern));
                }
            }
        } else {
            if (time == null) {
                time = System.currentTimeMillis() / 1000;
            }

            List<ServiceDay> serviceDays = RoutingContext.getServiceDays(index.graph, time);
            if (serviceDays == null)
                return patterns;
            Collection<TripPattern> tripPatterns = index.patternsForRoute.get(route);

            for (TripPattern pattern : tripPatterns) {
                Timetable timetable = pattern.scheduledTimetable;
                if (serviceDays.stream().anyMatch(sd -> timetable.temporallyViable(sd, time, lookoutSec, true))) {
                    patterns.add(new PatternDetail(pattern));
                }
            }
        }
        return patterns;
    }
}
