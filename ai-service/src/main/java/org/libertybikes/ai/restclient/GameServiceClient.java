/**
 *
 */
package org.libertybikes.ai.restclient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/")
public interface GameServiceClient {

    @GET
    @Path("/round/available")
    @Produces(MediaType.TEXT_HTML)
    public String getAvailableRounds();

    @GET
    @Path("/round/join/{roundId}")
    @Produces(MediaType.TEXT_HTML)
    public String joinRound(@PathParam("roundId") String id);

    @GET
    @Path("/round/ws/{roundId}")
    @Produces(MediaType.TEXT_HTML)
    public String joinSession(@PathParam("roundId") String id);

    @GET
    @Path("/party/{partyId}/round")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRoundId(@PathParam("partyId") String partyId);

}
