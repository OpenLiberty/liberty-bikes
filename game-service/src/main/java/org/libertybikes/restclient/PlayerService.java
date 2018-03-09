package org.libertybikes.restclient;

import javax.enterprise.context.Dependent;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Dependent
@RegisterRestClient
@Path("/player")
public interface PlayerService {

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Player getPlayerById(@PathParam("playerId") String id);

    @POST
    @Path("/{playerId}/win")
    public Response addWin(@PathParam("playerId") String id);

    @POST
    @Path("/{playerId}/loss")
    public Response addLoss(@PathParam("playerId") String id);

}
