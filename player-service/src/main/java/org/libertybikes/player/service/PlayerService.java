package org.libertybikes.player.service;

import java.util.Collection;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.libertybikes.player.data.PlayerDB;

@Path("/player")
@ApplicationScoped
public class PlayerService {

    @Inject
    PlayerDB db;

    @PostConstruct
    public void initPlayers() {
        // TODO use MP-Config to only run this for local dev mode
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            String id = createPlayer("SamplePlayer-" + i, null);
            for (int j = 0; j < 3; j++)
                recordGame(id, r.nextInt(4) + 1);
            for (int j = 0; j < 10; j++)
                recordGame(id, 4);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> getPlayers() {
        return db.getAll();
    }

    @POST
    @Path("/create")
    public String createPlayer(@QueryParam("name") String name, @QueryParam("id") String id) {
        Player p = new Player(name, id);
        if (db.create(p))
            System.out.println("Created a new player with name=" + name + " and id=" + p.id);
        else
            System.out.println("A player already existed with id=" + p.id);
        return p.id;
    }

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Player getPlayerById(@PathParam("playerId") String id) {
        Player p = db.get(id);
        if (p == null)
            System.out.println("Unable to find any player with id=" + id);
        return p;
    }

    @POST
    @Path("/{playerId}/recordGame")
    public void recordGame(@PathParam("playerId") String id, @QueryParam("place") int place) {
        Player p = getPlayerById(id);
        if (p == null)
            return;
        p.stats.totalGames++;
        switch (place) {
            case 1:
                p.stats.numWins++;
                p.stats.rating += 28;
                break;
            case 2:
                p.stats.rating += 14;
                break;
            case 3:
                p.stats.rating -= 5;
                break;
            default:
                p.stats.rating -= 12;
        }
        db.update(p);
        System.out.println(p);
    }
}
