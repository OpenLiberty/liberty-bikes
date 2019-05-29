package org.libertybikes.game.party;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.OutboundMessage;
import org.libertybikes.game.core.Player;
import org.libertybikes.game.metric.GameMetrics;

public class PartyQueue {

    private final ConcurrentLinkedDeque<QueuedClient> waitingPlayers = new ConcurrentLinkedDeque<>();
    private final Party party;

    public PartyQueue(Party p) {
        this.party = p;
    }

    public void add(String playerId, SseEventSink sink, Sse sse) {
        QueuedClient client = new QueuedClient(playerId, sink, sse);
        // If this client was already in the queue, remove them and add them at the end
        if (waitingPlayers.removeFirstOccurrence(client)) {
            party.log("Removed client " + playerId + " from queue before adding at end");
            GameMetrics.counterDec(GameMetrics.currentQueuedPlayersCounter);
        }
        party.log("Adding client " + playerId + " into the queue in position " + client.queuePosition());
        waitingPlayers.add(client);

        GameMetrics.counterInc(GameMetrics.currentQueuedPlayersCounter);

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
                GameMetrics.counterDec(GameMetrics.currentQueuedPlayersCounter);
            }
        }
        for (QueuedClient client : waitingPlayers)
            client.notifyPosition();
    }

    public void close() {
        party.log("Closing party queue");
        QueuedClient client = null;
        while ((client = waitingPlayers.pollFirst()) != null) {
            client.close();
            GameMetrics.counterDec(GameMetrics.currentQueuedPlayersCounter);
        }
    }

    private class QueuedClient {
        private final String playerId;
        private final SseEventSink sink;
        private final Sse sse;

        public QueuedClient(String playerId, SseEventSink sink, Sse sse) {
            this.playerId = playerId;
            this.sink = sink;
            this.sse = sse;
        }

        public int queuePosition() {
            int position = 1;
            for (QueuedClient c : PartyQueue.this.waitingPlayers) {
                if (this.equals(c))
                    break;
                else
                    position++;
            }
            return position;
        }

        public void notifyPosition() {
            int position = queuePosition();
            OutboundSseEvent event = sse.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .data(new OutboundMessage.QueuePosition(position))
                            .build();
            sink.send(event);
            party.log("Notified queued client " + playerId + " who is currently at position " + position);
        }

        public void promoteToGame(String roundId) {
            OutboundSseEvent event = sse.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .data(new OutboundMessage.RequeueGame(roundId))
                            .build();
            sink.send(event);
            party.log("Promoted queued client " + playerId + " into round " + roundId);
            close();
        }

        public void close() {
            sink.close();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (!(obj instanceof QueuedClient))
                return false;
            QueuedClient other = (QueuedClient) obj;
            return Objects.equals(this.playerId, other.playerId);
        }
    }

}
