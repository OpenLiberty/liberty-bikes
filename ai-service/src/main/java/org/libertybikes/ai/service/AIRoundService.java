/**
 *
 */
package org.libertybikes.ai.service;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.libertybikes.ai.restclient.GameServiceClient;

@ApplicationScoped
@Path("open")
public class AIRoundService {

    // TODO - url prop not working
//    @Inject
//    @RestClient
//    GameServiceClient client;

    private GameServiceClient getClient() {
        try {
            return RestClientBuilder.newBuilder().baseUrl(new URL("http://localhost:8080")).build(GameServiceClient.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/join/{partyId}")
    public String joinSession(@PathParam("partyId") String partyId) {
        System.out.println("Trying to join " + partyId + " party.");

        // get party's current round
        String roundId = getClient().getRoundId(partyId);

        System.out.println("got current round for party: " + roundId);

        // open a websocket and join it.
        AIWebSocket socket = new AIWebSocket(roundId);
        return "successful?";
    }

}
