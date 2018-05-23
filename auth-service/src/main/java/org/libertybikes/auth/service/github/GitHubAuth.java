package org.libertybikes.auth.service.github;

import java.net.URI;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.libertybikes.auth.service.AuthApp;

@Path("/GitHubAuth")
@ApplicationScoped
public class GitHubAuth {

    private final static String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";

    @Inject
    @ConfigProperty(name = "github_key")
    String key;

    @Inject
    @ConfigProperty(name = "auth_url", defaultValue = AuthApp.HTTPS_AUTH_SERVICE)
    String authUrl;

    @GET
    public Response getAuthURL(@Context HttpServletRequest request) {
        try {
            String randomCode = UUID.randomUUID().toString();
            request.getSession().setAttribute("github", randomCode);

            // GitHub will tell the users browser to go to this address once they are done authing.
            String callbackURL = authUrl + "/GitHubCallback";

            // send the user to GitHub to be authenticated
            String redirectURL = GITHUB_AUTH_URL + "?client_id=" + key + "&redirect_url=" + callbackURL + "&scope=user:email&state=" + randomCode;
            return Response.temporaryRedirect(new URI(redirectURL)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
    }

}
