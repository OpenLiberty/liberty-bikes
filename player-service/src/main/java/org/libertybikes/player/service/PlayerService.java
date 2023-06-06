package org.libertybikes.player.service;

import java.util.Collection;
import java.util.HashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.libertybikes.player.data.PlayerDB;

@Path("/player")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PlayerService {

    @Inject
    PlayerDB db;

    @Inject
    private JsonWebToken jwt;

    @GET
    public Collection<Player> getPlayers() {
        return db.getAll();
    }

    @Inject
    private MetricRegistry registry;

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Counted(name = "num_player_logins", description = "Number of Total Logins", absolute = true)
    public String createPlayer(@QueryParam("name") String name, @QueryParam("id") String id) {
        // Validate player name
        if (name == null)
            return null;
        name = name.replaceAll("[^\\x61-\\x7A\\x41-\\x5A\\x30-\\x39\\xC0-\\xFF -]", "").trim();
        if (name.length() == 0)
            return null;
        if (name.length() > 20)
            name = name.substring(0, 20);

        Player p = new Player(name, id);
        if (db.create(p))
            System.out.println("Created a new player with id=" + p.id);
        else
            System.out.println("A player already existed with id=" + p.id);
        return p.id;
    }

    @GET
    @Path("/{playerId}")
    public Player getPlayerById(@PathParam("playerId") String id) {
        if (id == null)
            return null;
        Player p = db.get(id);
        if (p == null)
            System.out.println("Unable to find any player with id=" + id);
        return p;
    }

    @GET
    @Path("/getJWTInfo")
    public HashMap<String, String> getJWTInfo() {

        HashMap<String, String> map = new HashMap<String, String>();

        String id = jwt.getClaim("id");
        if (db.exists(id)) {
            map.put("exists", "true");
            map.put("username", db.get(id).name);

        } else {
            map.put("exists", "false");
        }
        map.put("id", id);
        return map;
    }
}
