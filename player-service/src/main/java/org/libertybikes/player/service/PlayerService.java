package org.libertybikes.player.service;

import java.util.Collection;
import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetadataBuilder;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
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

    private static final Metadata numLoginsCounter = new MetadataBuilder()
                    .withName("num_player_logins")
                    .withDisplayName("Number of Total Logins")
                    .withDescription("How many times a user has logged in.")
                    .withType(MetricType.COUNTER)
                    .build();

    @POST
    @Produces(MediaType.TEXT_HTML)
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

        if (id != null && registry != null) {
            registry.counter(numLoginsCounter).inc();
        }
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
