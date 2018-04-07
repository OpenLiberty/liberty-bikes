package org.libertybikes.player.service;

import java.util.Collection;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.libertybikes.player.data.PlayerDB;

@Path("/rank")
@ApplicationScoped
public class RankingService {

    @Inject
    PlayerDB db;

    @GET
    @Path("/top")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> topFivePlayers() {
        return topNPlayers(5);
    }

    @GET
    @Path("/top/{numPlayers}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> topNPlayers(@PathParam("numPlayers") Integer numPlayers) {
        if (numPlayers < 0)
            return Collections.emptySet();
        if (numPlayers > 100)
            numPlayers = 100;
        return db.topPlayers(numPlayers);
    }

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public long getRank(@PathParam("playerId") String id) {
        return db.getRank(id);
    }

}
