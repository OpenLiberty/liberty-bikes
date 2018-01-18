package org.libertybikes.player.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class PlayerService {

    Map<String, Player> allPlayers = new HashMap<>();

    @PostConstruct
    public void initPlayers() {
        for (int i = 0; i < 10; i++)
            createPlayer();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> getPlayers() {
        return allPlayers.values();
    }

    @GET
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createPlayer() {
        Player p = new Player();
        p.id = UUID.randomUUID().toString();
        p.name = "Bob";
        allPlayers.put(p.id, p);
        return p.id;
    }

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Player getPlayerById(@PathParam("playerId") String id) {
        return allPlayers.get(id);
    }

}
