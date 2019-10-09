package org.libertybikes.game.round.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.libertybikes.game.core.GameRound;

@Path("/round")
@ApplicationScoped
public class GameRoundService {

    @Inject
    @ConfigProperty(name = "singleParty", defaultValue = "true")
    private boolean isSingleParty;

    @Resource
    ManagedScheduledExecutorService exec;

    private final Map<String, GameRound> allRounds = new ConcurrentHashMap<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<GameRound> listAllGames() {
        return allRounds.values();
    }

    @POST
    public String createRound() {
        GameRound p = new GameRound();
        allRounds.put(p.id, p);
        System.out.println("Created round id=" + p.id);
        if (allRounds.size() > 35)
            System.out.println("WARNING: Found " + allRounds.size() + " active games in GameRoundService. " +
                               "They are probably not being cleaned up properly: " + allRounds.keySet());
        return p.id;
    }

    @POST
    public GameRound createRoundById(@QueryParam("gameId") String gameId) {
        GameRound round = allRounds.computeIfAbsent(gameId, k -> new GameRound(gameId));
        System.out.println("Created round id=" + round.id);
        if (allRounds.size() > 35)
            System.out.println("WARNING: Found " + allRounds.size() + " active games in GameRoundService. " +
                               "They are probably not being cleaned up properly: " + allRounds.keySet());
        return round;
    }

    @GET
    @Path("/available")
    public String getAvailableRound() {
        if (isSingleParty)
            throw new NotAcceptableException("Cannot call this endpoint when game service is in single party mode");

        Optional<GameRound> availableRound = allRounds.values()
                        .stream()
                        .filter(r -> r.isOpen())
                        .findFirst();
        if (availableRound.isPresent())
            return availableRound.get().id;
        else
            return createRound();
    }

    @GET
    @Path("/{roundId}")
    @Produces(MediaType.APPLICATION_JSON)
    public GameRound getRound(@PathParam("roundId") String roundId) {
        return allRounds.get(roundId);
    }

    @GET
    @Path("/{roundId}/requeue")
    public String requeue(@PathParam("roundId") String oldRoundId, @QueryParam("isPlayer") boolean isPlayer) {
        GameRound oldRound = getRound(oldRoundId);

        // Do not allow anyone to skip ahead past a round that has not started yet
        if (oldRound == null || !oldRound.isStarted())
            return null;

        GameRound nextRound = createRoundById(oldRound.nextRoundId);

        // If player tries to requeue and next game is already in progress, requeue ahead to the next game
        if (isPlayer && nextRound.isStarted())
            return requeue(nextRound.id, isPlayer);
        // If next round is already done, requeue ahead to next game
        else if (nextRound.getGameState() == GameRound.State.FINISHED)
            return requeue(nextRound.id, isPlayer);
        else
            return nextRound.id;
    }

    public void deleteRound(GameRound round) {
        String roundId = round.id;
        if (round.isOpen())
            round.endGame();
        System.out.println("Scheduling round id=" + roundId + " for deletion in 15 minutes");
        // Do not immediately delete rounds in order to give players/spectators time to move along to the next game
        exec.schedule(() -> {
            allRounds.remove(roundId);
            System.out.println("Deleted round id=" + roundId);
        }, 15, TimeUnit.MINUTES);
    }

}
