package org.libertybikes.auth.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * Servlet implementation class googleCallback
 */

@Path("/GoogleCallback")
@ApplicationScoped
public class GoogleCallback extends JwtAuth {
    private static final long serialVersionUID = 1L;

    @Context
    HttpServletRequest request;

    @Inject
    @ConfigProperty(name = "frontend_url", defaultValue = AuthApp.FRONTEND_URL)
    String frontendUrl;

    @Inject
    @ConfigProperty(name = "auth_url", defaultValue = AuthApp.HTTPS_AUTH_SERVICE)
    String authUrl;

    private GoogleAuthorizationCodeFlow flow = null;

    /**
     * Method that performs introspection on an AUTH string, and returns data as
     * a String->String hashmap.
     *
     * @param auth
     *            the authstring to query, as built by an auth impl.
     * @return the data from the introspect, in a map.
     * @throws IOException
     *             if anything goes wrong.
     */
    public Map<String, String> introspectAuth(GoogleTokenResponse gResponse) throws IOException {
        Map<String, String> results = new HashMap<String, String>();

        Credential credential = flow.createAndStoreCredential(gResponse, null);
        System.out.println(credential.toString());

        try {
            // ask google to verify the response from the auth string
            // if invalid, it'll throw an exception
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credential);
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpRequest infoRequest = requestFactory.buildGetRequest(url);

            infoRequest.getHeaders().setContentType("application/json");
            String jsonIdentity = infoRequest.execute().parseAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode user = objectMapper.readTree(jsonIdentity);

            String name = user.get("name").asText();
            String email = user.get("email").asText();

            results.put("valid", "true");
            results.put("id", "GOOGLE:" + email);
            results.put("upn", email);
            results.put("name", name);
            results.put("email", email);

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            results.put("valid", "false");
        }

        return results;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthURL() throws IOException, URISyntaxException {
        // google calls us back at this app when a user has finished authing with them.
        // when it calls us back here, it passes an oauth_verifier token that we
        // can exchange for a google access token.

        flow = (GoogleAuthorizationCodeFlow) request.getSession().getAttribute("google");
        String code = request.getParameter("code");

        //now we need to invoke the access_token endpoint to swap the code for a token.
        String callbackURL = authUrl + "/GoogleCallback";

        GoogleTokenResponse gResponse;
        Map<String, String> claims = new HashMap<String, String>();
        try {
            gResponse = flow.newTokenRequest(code).setRedirectUri(callbackURL.toString()).execute();
            claims = introspectAuth(gResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if auth key was no longer valid, we won't build a JWT. Redirect back to start.
        if (!"true".equals(claims.get("valid"))) {
            return Response.temporaryRedirect(new URI(frontendUrl)).build();
        } else {
            String newJwt = createJwt(claims);
            return Response.temporaryRedirect(new URI(frontendUrl + "/" + newJwt)).build();
        }
    }
}