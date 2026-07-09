package org.libertybikes.auth.service.github;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.libertybikes.auth.service.ConfigBean;
import org.libertybikes.auth.service.JwtAuth;
import org.libertybikes.auth.service.github.GitHubOAuthAPI.GithubTokenResponse;
import org.libertybikes.auth.service.github.GitHubUserAPI.EmailData;

@Path("/GitHubCallback")
@ApplicationScoped
public class GitHubCallback extends JwtAuth {

    @Inject
    GitHubOAuthAPI githubOAuthAPI;

    @Inject
    GitHubUserAPI githubUserAPI;

    @Inject
    ConfigBean config;

    @Inject
    Validator validator;

    // TODO: need to manually validate return types because the @Valid annotation on our MP Rest Client interface
    // is getting lost when the instance is injected into this class.
    private void validate(Object obj) {
        Objects.requireNonNull(obj);
        Set<ConstraintViolation<Object>> issues = validator.validate(obj);
        if (!issues.isEmpty()) {
            throw new ConstraintViolationException(issues);
        }
    }

    @GET
    @Counted(name = "num_github_logins",
             description = "How many times a user has logged in through Github Auth.")
    public Response getGitHubCallbackURL(@Context HttpServletRequest request) throws URISyntaxException {
        try {
            String githubCode = request.getParameter("code");
            String randomCode = (String) request.getSession().getAttribute("github");

            String thisURL = config.authUrl + "/GitHubCallback";

            // First send the user through GitHub OAuth to get permission to read their email address
            GithubTokenResponse response = githubOAuthAPI.accessToken(config.github_key, config.github_secret, githubCode, thisURL, randomCode);
            validate(response);
            System.out.println("GitHub access token: " + response.access_token);

            // Once we have the access token, use it to read their email
            EmailData[] emails = githubUserAPI.getEmail(response.access_token);
            for (EmailData email : emails)
                validate(email);
            String primaryEmail = null;
            for (EmailData data : emails)
                if (data.primary) {
                    primaryEmail = data.email;
                    break;
                } else {
                    primaryEmail = data.email;
                }
            System.out.println("Got primary email of: " + primaryEmail);

            Map<String, String> claims = new HashMap<String, String>();
            claims.put("valid", "true");
            claims.put("id", "GITHUB:" + primaryEmail);
            claims.put("upn", primaryEmail);
            claims.put("email", primaryEmail);
            return Response.temporaryRedirect(new URI(config.frontendUrl + "/" + createJwt(claims))).build();
        } catch (Exception e) {
            e.printStackTrace();
            return fail();
        }
    }

    private Response fail() throws URISyntaxException {
        return Response.temporaryRedirect(new URI(config.frontendUrl)).build();
    }
}