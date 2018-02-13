package org.libertybikes.game.round.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.GameRound.State;

@Path("/")
@ApplicationScoped
public class GameRoundService {

    @Context
    UriInfo uri;

    private final Map<String, GameRound> allRounds = new HashMap<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<GameRound> listAllGames() {
        return allRounds.values();
    }

    @POST
    @Path("/joinRound/{roundId}")
    public String joinRound(@PathParam("roundId") String roundId) {
        GameRound r = allRounds.get(roundId.toUpperCase());
        if (r == null) {
            return "Game Does Not Exist";
        }
        if (r.state.equals(State.FULL)) {
            return "Game Is Full";
        }
        if (!r.state.equals(State.OPEN)) {
            return "Game Already Started";
        }
        return "VALID";
    }

    @POST
    @Path("/create")
    public String createRound() {
        GameRound p = new GameRound();
        allRounds.put(p.id, p);
        System.out.println("Created round id=" + p.id);
        if (allRounds.size() > 3)
            System.out.println("WARNING: Found more than 3 active games in GameRoundService. " +
                               "They are probably not being cleaned up properly: " + allRounds.keySet());
        return p.id;
    }

    @GET
    @Path("/{roundId}")
    @Produces(MediaType.APPLICATION_JSON)
    public GameRound getRound(@PathParam("roundId") String roundId) {
        return allRounds.get(roundId);
    }

    public GameRound requeue(GameRound oldRound) {
        GameRound nextRound = new GameRound(oldRound.nextRoundId);
        GameRound existingRound = allRounds.putIfAbsent(oldRound.nextRoundId, nextRound);
        return existingRound == null ? nextRound : existingRound;
    }

    public GameRound deleteRound(String roundId) {
        System.out.println("Deleting round id=" + roundId);
        return allRounds.remove(roundId);
    }

}
