package org.libertybikes.auth.service.github;

import java.net.URI;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import org.libertybikes.auth.service.ConfigBean;

@Path("/GitHubAuth")
@ApplicationScoped
public class GitHubAuth {

    private final static String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";

    @Inject
    ConfigBean config;

    @GET
    public Response getGitHubAuthURL(@Context HttpServletRequest request) {
        try {
            String randomCode = UUID.randomUUID().toString();
            request.getSession().setAttribute("github", randomCode);

            // GitHub will tell the users browser to go to this address once they are done authing.
            String callbackURL = config.authUrl + "/GitHubCallback";

            // send the user to GitHub to be authenticated
            String redirectURL = GITHUB_AUTH_URL + "?client_id=" + config.github_key + "&redirect_url=" + callbackURL + "&scope=user:email&state=" + randomCode;
            return Response.temporaryRedirect(new URI(redirectURL)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
    }

}
