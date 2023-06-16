package org.libertybikes.restclient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
