package org.libertybikes.player.service;

import java.util.Collection;

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
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> topPlayers() {
        return db.topPlayers(5);
    }

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public long getRank(@PathParam("playerId") String id) {
        return db.getRank(id);
    }

}
