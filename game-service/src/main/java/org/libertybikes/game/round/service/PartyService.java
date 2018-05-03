/**
 *
 */
package org.libertybikes.game.round.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.libertybikes.game.party.Party;

@Path("/party")
@ApplicationScoped
public class PartyService {

    // Map of PartyID to current RoundID
    private final Map<String, Party> allParties = new ConcurrentHashMap<>();

    @Resource
    private ManagedScheduledExecutorService exec;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Party> listParties() {
        return Collections.unmodifiableCollection(allParties.values());
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    public Party createParty() {
        Party p = CDI.current().select(Party.class).get();
        allParties.put(p.id, p);
        // Put a max lifetime of 12 hours on a party
        exec.schedule(() -> this.deleteParty(p.id), 12, TimeUnit.HOURS);
        return p;
    }

    @GET
    @Path("/{partyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Party getParty(@PathParam("partyId") String partyId) {
        if (partyId == null) {
            System.out.println("WARN: got null partyId request");
            return null;
        }
        return allParties.get(partyId.toUpperCase());
    }

    public void deleteParty(String partyId) {
        Party deleted = allParties.remove(partyId);
        if (deleted != null) {
            deleted.close();
            System.out.println("Deleted party " + partyId);
        }
    }

    @GET
    @Path("/{partyId}/round")
    public String getCurrentRound(@PathParam("partyId") String partyId) {
        Party p = getParty(partyId);
        return p == null ? null : p.getCurrentRound().id;
    }

    @GET
    @Path("/{partyId}/queue")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void joinQueue(@PathParam("partyId") String partyId, @Context SseEventSink sink, @Context Sse sse) {
        Party p = getParty(partyId);
        if (p != null)
            p.enqueueClient(sink, sse);
    }

}
