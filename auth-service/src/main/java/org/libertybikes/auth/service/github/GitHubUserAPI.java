package org.libertybikes.auth.service.github;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/user")
public interface GitHubUserAPI {

    public static class EmailData {
        @Email
        public String email;
        public boolean primary;
    }

    @GET
    @Path("/emails")
    @Produces(MediaType.APPLICATION_JSON)
    @Valid // TODO: beanval isn't being called here when rest client instance is injected
    public EmailData[] getEmail(@QueryParam("access_token") String accessToken);

}
