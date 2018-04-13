/**
 *
 */
package org.libertybikes.game.round.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/party")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PartyService {

    // Map of PartyID to current RoundID
    private final Map<String, Party> allParties = new ConcurrentHashMap<>();

    @GET
    public Collection<Party> listParties() {
        return allParties.values();
    }

    @POST
    @Path("/create")
    public Party createParty() {
        Party p = CDI.current().select(Party.class).get();
        allParties.put(p.id, p);
        return p;
    }

    @GET
    @Path("/{partyId}")
    public Party getParty(@PathParam("partyId") String partyId) {
        if (partyId == null)
            return null;
        return allParties.get(partyId.toUpperCase());
    }

    @GET
    @Path("/{partyId}/round")
    public String getCurrentRound(@PathParam("partyId") String partyId) {
        if (partyId == null)
            return null;
        Party p = getParty(partyId.toUpperCase());
        return p == null ? null : p.getCurrentRound().id;
    }

}
