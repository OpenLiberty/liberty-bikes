package org.libertybikes.auth.service.google;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.libertybikes.auth.service.ConfigBean;
import org.libertybikes.auth.service.JwtAuth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

@Path("/GoogleCallback")
@ApplicationScoped
public class GoogleCallback extends JwtAuth {

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Inject
    ConfigBean config;

    public static class GoogleUser {
        public String name;
        public String email;
    }

    /**
     * Method that performs introspection on an AUTH string, and returns data as
     * a String->String hashmap.
     *
     * @param auth
     *            the authstring to query, as built by an auth impl.
     * @return the data from the introspect, in a map.
     */
    private Map<String, String> introspectAuth(GoogleAuthorizationCodeFlow flow, GoogleTokenResponse gResponse) throws IOException {
        Map<String, String> results = new HashMap<String, String>();

        Credential credential = flow.createAndStoreCredential(gResponse, null);

        try {
            // ask google to verify the response from the auth string
            // if invalid, it'll throw an exception
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credential);
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpRequest infoRequest = requestFactory.buildGetRequest(url);

            infoRequest.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String jsonIdentity = infoRequest.execute().parseAsString();
            GoogleUser user = jsonb.fromJson(jsonIdentity, GoogleUser.class);
            System.out.println("User logged in: " + jsonb.toJson(user));
            Objects.requireNonNull(user.name, "User name was null");
            Objects.requireNonNull(user.email, "User email was null");

            results.put("valid", "true");
            results.put("id", "GOOGLE:" + user.email);
            results.put("upn", user.email);
            results.put("name", user.name);
            results.put("email", user.email);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            results.put("valid", "false");
        }

        return results;
    }

    @GET
    @Counted(name = "num_google_logins",
             displayName = "Number of Google Logins",
             description = "How many times a user has logged in through Google Auth.",
             absolute = true)
    public Response getGoogleAuthURL(@Context HttpServletRequest request) throws IOException, URISyntaxException {
        // google calls us back at this app when a user has finished authing with them.
        // when it calls us back here, it passes an oauth_verifier token that we
        // can exchange for a google access token.

        GoogleAuthorizationCodeFlow flow = (GoogleAuthorizationCodeFlow) request.getSession().getAttribute("google");
        if (flow == null)
            return failureRedirect("did not find 'google' attribute set in HTTP session. It should be set by GoogleAuth");
        String code = request.getParameter("code");

        //now we need to invoke the access_token endpoint to swap the code for a token.
        String callbackURL = config.authUrl + "/GoogleCallback";

        Map<String, String> claims = new HashMap<String, String>();
        try {
            GoogleAuthorizationCodeTokenRequest token = flow.newTokenRequest(code).setRedirectUri(callbackURL);
            GoogleTokenResponse gResponse = token.execute();
            claims.putAll(introspectAuth(flow, gResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if auth key was no longer valid, we won't build a JWT. Redirect back to start.
        if (!"true".equals(claims.get("valid"))) {
            return failureRedirect("claim was not valid");
        } else {
            String newJwt = createJwt(claims);
            return Response.temporaryRedirect(new URI(config.frontendUrl + "/" + newJwt)).build();
        }
    }

    private Response failureRedirect(String reason) throws URISyntaxException {
        System.out.println("Google auth failed because " + reason);
        return Response.temporaryRedirect(new URI(config.frontendUrl)).build();
    }
}