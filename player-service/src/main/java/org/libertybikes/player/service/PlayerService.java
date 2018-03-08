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
import javax.ws.rs.core.Response;

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
            String id = createPlayer("Bob-" + i);
            int wins = r.nextInt(10);
            int losses = r.nextInt(10);
            for (int w = 0; w < wins; w++)
                addWin(id);
            for (int l = 0; l < losses; l++)
                addLoss(id);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> getPlayers() {
        return db.getAll();
    }

    @GET
    @POST
    @Path("/create")
    public String createPlayer(@QueryParam("name") String name) {
        Player p = new Player(name);
        db.put(p);
        return p.id;
    }

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Player getPlayerById(@PathParam("playerId") String id) {
        return db.get(id);
    }

    @GET
    @Path("/{playerId}/win")
    public Response addWin(@PathParam("playerId") String id) {
        Player p = getPlayerById(id);
        if (p == null)
            return Response.status(404).build();
        int wins = p.stats.numWins++;
        p.stats.totalGames++;
        db.put(p);
        System.out.println("Player " + id + " has won " + p.stats.numWins + " games and played in " + p.stats.totalGames + " games.");
        return Response.ok(wins).build();
    }

    @GET
    @Path("/{playerId}/loss")
    public Response addLoss(@PathParam("playerId") String id) {
        Player p = getPlayerById(id);
        if (p == null)
            return Response.status(404).build();
        int games = p.stats.totalGames++;
        db.put(p);
        System.out.println("Player " + id + " has won " + p.stats.numWins + " games and played in " + p.stats.totalGames + " games.");
        return Response.ok(games).build();
    }

}
