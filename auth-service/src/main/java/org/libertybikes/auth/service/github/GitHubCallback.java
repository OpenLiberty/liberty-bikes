package org.libertybikes.auth.service.github;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.libertybikes.auth.service.AuthApp;
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
    @ConfigProperty(name = "github_key")
    String key;

    @Inject
    @ConfigProperty(name = "github_secret")
    String secret;

    @Inject
    @ConfigProperty(name = "frontend_url", defaultValue = AuthApp.FRONTEND_URL)
    String frontendUrl;

    @Inject
    @ConfigProperty(name = "auth_url", defaultValue = AuthApp.HTTPS_AUTH_SERVICE)
    String authUrl;

    @Inject
    Validator validator;

    @GET
    @Path("/test")
    @Valid
    public GithubTokenResponse testToken() {
        for (Method m : getClass().getMethods()) {
            System.out.println("@AGG found method: " + m.getName());
            for (Annotation a : m.getAnnotations())
                System.out.println("  anno=" + a);
        }
        GithubTokenResponse badToken = new GithubTokenResponse();
        badToken.access_token = "bogus";
        return badToken;
    }

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
    public Response getAuthURL(@Context HttpServletRequest request) throws IOException, URISyntaxException {
        try {
            String githubCode = request.getParameter("code");
            String randomCode = (String) request.getSession().getAttribute("github");

            String thisURL = authUrl + "/GitHubCallback";

            // First send the user through GitHub OAuth to get permission to read their email address
            GithubTokenResponse response = githubOAuthAPI.accessToken(key, secret, githubCode, thisURL, randomCode);
//            for (Method m : githubOAuthAPI.getClass().getMethods()) {
//                System.out.println("@AGG found method: " + m.getName());
//                for (Annotation a : m.getAnnotations())
//                    System.out.println("  anno=" + a);
//            }
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
            return Response.temporaryRedirect(new URI(frontendUrl + "/" + createJwt(claims))).build();
        } catch (Exception e) {
            e.printStackTrace();
            return fail();
        }
    }

    private Response fail() throws URISyntaxException {
        return Response.temporaryRedirect(new URI(frontendUrl)).build();
    }
}