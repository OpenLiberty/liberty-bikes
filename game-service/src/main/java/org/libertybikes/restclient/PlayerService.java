package org.libertybikes.restclient;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "http://localhost:8081/")
@Path("/")
public interface PlayerService {

    @GET
    @Path("/player/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true) // hide operation from OpenAPI
    public Player getPlayerById(@PathParam("playerId") String id);

    @POST
    @Path("/rank/{playerId}")
    @Operation(hidden = true) // hide operation from OpenAPI
    public void recordGame(@PathParam("playerId") String id, @QueryParam("place") int place, @HeaderParam("Authorization") String token);

}
