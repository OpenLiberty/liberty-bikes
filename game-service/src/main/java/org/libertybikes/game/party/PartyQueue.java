package org.libertybikes.game.party;

import java.util.concurrent.ConcurrentLinkedDeque;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.OutboundMessage;
import org.libertybikes.game.core.Player;

public class PartyQueue {

    private final ConcurrentLinkedDeque<QueuedClient> waitingPlayers = new ConcurrentLinkedDeque<>();
    private final Party party;
    private int queueCounter = 0, firstClient = 0;

    public PartyQueue(Party p) {
        this.party = p;
    }

    public void add(SseEventSink sink, Sse sse) {
        QueuedClient client = new QueuedClient(sink, sse);
        party.log("Adding client " + client.queueNumber + " into the queue in position " + client.queuePosition());
        waitingPlayers.add(client);
        if (party.getCurrentRound().isOpen())
            promoteClients();
        else
            client.notifyPosition();
    }

    public void promoteClients() {
        GameRound newRound = party.getCurrentRound();
        int availableSpots = Player.MAX_PLAYERS - newRound.getPlayers().size();
        for (int i = 0; i < availableSpots; i++) {
            QueuedClient first = waitingPlayers.pollFirst();
            if (first != null) {
                first.promoteToGame(newRound.id);
                firstClient++;
            }
        }
        for (QueuedClient client : waitingPlayers)
            client.notifyPosition();
    }

    public void close() {
        party.log("Closing party queue");
        for (QueuedClient client : waitingPlayers)
            client.close();
    }

    private class QueuedClient {
        private final int queueNumber;
        private final SseEventSink sink;
        private final Sse sse;

        public QueuedClient(SseEventSink sink, Sse sse) {
            this.sink = sink;
            this.sse = sse;
            this.queueNumber = PartyQueue.this.queueCounter++;
        }

        public int queuePosition() {
            return this.queueNumber - PartyQueue.this.firstClient + 1;
        }

        public void notifyPosition() {
            OutboundSseEvent event = sse.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .data(new OutboundMessage.QueuePosition(queuePosition()))
                            .build();
            party.log("Notifying queued client " + queueNumber + " who is currently at position " + queuePosition());
            sink.send(event);
        }

        public void promoteToGame(String roundId) {
            OutboundSseEvent event = sse.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .data(new OutboundMessage.RequeueGame(roundId))
                            .build();
            party.log("Promoting queued client " + queueNumber + " into round " + roundId);
            sink.send(event);
            close();
        }

        public void close() {
            sink.close();
        }
    }

}
