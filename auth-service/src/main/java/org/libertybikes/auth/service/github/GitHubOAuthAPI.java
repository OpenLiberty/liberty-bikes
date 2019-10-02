package org.libertybikes.auth.service.github;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://github.com")
@Path("/login/oauth")
public interface GitHubOAuthAPI {

    public static class GithubTokenResponse {
        public String access_token;
    }

    @GET
    @Path("/access_token")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true) // hide operation from OpenAPI
    public GithubTokenResponse accessToken(@QueryParam("client_id") String key,
                                           @QueryParam("client_secret") String secret,
                                           @QueryParam("code") String code,
                                           @QueryParam("redirect_uri") String redirect_uri,
                                           @QueryParam("state") String state);

}
