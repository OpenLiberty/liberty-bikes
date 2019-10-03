package org.libertybikes.player.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.libertybikes.player.data.PlayerDB;

@Path("/rank")
@ApplicationScoped
public class RankingService {

    public static final int RATING_INITIAL = 1000;
    private static final int[] RATING_CHANGES = { 28, 14, -5, -12 };

    @Inject
    PlayerDB db;

    @Inject
    PlayerService playerSvc;

    @PostConstruct
    public void initPlayers() {
        if (db.exists(Player.createDefaultId("SamplePlayer-0"))) {
            System.out.println("Sample players already exist in database.");
            return;
        }

        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            String id = playerSvc.createPlayer("SamplePlayer-" + i, null);
            for (int j = 0; j < 3; j++)
                recordGameInternal(id, r.nextInt(4) + 1);
            for (int j = 0; j < 10; j++)
                recordGameInternal(id, 4);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> topNPlayers(@QueryParam("limit") @DefaultValue("5") Integer numPlayers) {
        if (numPlayers < 0)
            return Collections.emptySet();
        if (numPlayers > 100)
            numPlayers = 100;
        return db.topPlayers(numPlayers);
    }

    @POST
    @RolesAllowed({ "admin" })
    @Path("/{playerId}")
    public void recordGame(@PathParam("playerId") String id, @QueryParam("place") int place, @HeaderParam("Authorization") String token) {
        recordGameInternal(id, place);
    }

    void recordGameInternal(String id, int place) {
        if (place < 1 || place > 4) {
            System.out.println("Invalid place (" + place + "), must be 1-4");
            return;
        }
        Player p = playerSvc.getPlayerById(id);
        if (p == null)
            return;
        p.stats.totalGames++;
        p.stats.rating += ratingChange(place);
        if (place == 1)
            p.stats.numWins++;
        db.update(p);
        System.out.println(p);
    }

    public static int ratingChange(int place) {
        if (place < 1 || place > 4)
            return 0;
        return RATING_CHANGES[place - 1];
    }

}
