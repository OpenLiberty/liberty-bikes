package org.libertybikes.auth.service.github;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://api.github.com")
@Path("/user")
public interface GitHubUserAPI {

    public static class EmailData {
        public String email;
        public boolean primary;
    }

    @GET
    @Path("/emails")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true) // hide operation from OpenAPI
    public EmailData[] getEmail(@QueryParam("access_token") String accessToken);

}
