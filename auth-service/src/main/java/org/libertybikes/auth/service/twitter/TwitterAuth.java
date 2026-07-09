package org.libertybikes.auth.service.twitter;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import org.libertybikes.auth.service.ConfigBean;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

@Path("/TwitterAuth")
@ApplicationScoped
public class TwitterAuth {

    @Inject
    ConfigBean config;

    @GET
    public Response getTwitterAuthURL(@Context HttpServletRequest request) {

        ConfigurationBuilder c = new ConfigurationBuilder()
                        .setOAuthConsumerKey(config.twitter_key)
                        .setOAuthConsumerSecret(config.twitter_secret);

        Twitter twitter = new TwitterFactory(c.build()).getInstance();
        request.getSession().setAttribute("twitter", twitter);

        try {
            String callbackURL = config.authUrl + "/TwitterCallback";

            RequestToken token = twitter.getOAuthRequestToken(callbackURL);
            request.getSession().setAttribute("twitter_token", token);

            // send the user to Twitter to be authenticated
            String authorizationUrl = token.getAuthenticationURL();
            return Response.temporaryRedirect(new URI(authorizationUrl)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
    }

}
