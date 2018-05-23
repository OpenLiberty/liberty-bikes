package org.libertybikes.auth.service.github;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/login/oauth")
public interface GitHubOAuthAPI {

    public static class GithubTokenResponse {
        @NotNull
        public String access_token;
    }

    @GET
    @Path("/access_token")
    @Produces(MediaType.APPLICATION_JSON)
    @Valid // TODO: beanval isn't being honored here when used with MP Rest client
    public GithubTokenResponse accessToken(@QueryParam("client_id") String key,
                                           @QueryParam("client_secret") String secret,
                                           @QueryParam("code") String code,
                                           @QueryParam("redirect_uri") String redirect_uri,
                                           @QueryParam("state") String state);

}
