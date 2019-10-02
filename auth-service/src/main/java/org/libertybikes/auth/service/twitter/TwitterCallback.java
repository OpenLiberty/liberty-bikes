package org.libertybikes.auth.service.twitter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.libertybikes.auth.service.ConfigBean;
import org.libertybikes.auth.service.JwtAuth;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

@Path("/TwitterCallback")
@ApplicationScoped
public class TwitterCallback extends JwtAuth {

    @Inject
    ConfigBean config;

    @GET
    @Counted(name = "num_twitter_logins",
             displayName = "Number of Twitter Logins",
             description = "How many times a user has logged in through Twitter Auth.",
             absolute = true)
    public Response getTwitterCallbackURL(@Context HttpServletRequest request) throws URISyntaxException {
        try {
            Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
            RequestToken reqToken = (RequestToken) request.getSession().getAttribute("twitter_token");

            String verifier = request.getParameter("oauth_verifier");
            if (verifier == null)
                return fail();

            request.getSession().removeAttribute("twitter_token");

            AccessToken accessToken = twitter.getOAuthAccessToken(reqToken, verifier);

            ConfigurationBuilder configBuilder = new ConfigurationBuilder()
                            .setOAuthConsumerKey(config.twitter_key)
                            .setOAuthConsumerSecret(config.twitter_secret)
                            .setOAuthAccessToken(accessToken.getToken())
                            .setOAuthAccessTokenSecret(accessToken.getTokenSecret())
                            .setIncludeEmailEnabled(true)
                            .setJSONStoreEnabled(true);
            twitter = new TwitterFactory(configBuilder.build()).getInstance();

            User user = twitter.verifyCredentials();
            long twitterID = user.getId();
            String name = user.getScreenName();
            System.out.println("Got twitter ID=" + twitterID + " with name=" + name);

            Map<String, String> claims = new HashMap<String, String>();
            claims.put("valid", "true");
            claims.put("id", "TWITTER:" + twitterID);
            claims.put("upn", "" + name);
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