package org.libertybikes.auth.service.github;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
