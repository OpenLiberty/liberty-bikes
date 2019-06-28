/**
 *
 */
package org.libertybikes.ai;

import org.libertybikes.ai.service.AIWebSocket;
import org.libertybikes.ai.service.AIWebSocket.DIRECTION;

public class AIClass {

    int startX, startY;
    AIWebSocket socket;

    public AIClass(AIWebSocket socket, int x, int y) {
        this.socket = socket;
        startX = x;
        startY = y;
    }

    int count = 0;

    /*
     * This should contain the main logic of the AI, it receives a JSON of all the
     * objects on the board
     * TODO: describe possible values in json
     */
    public void processAiMove(String message) {

        count++;
        int mod = count % 4;
        switch (mod) {
            case 0:
                socket.sendDirection(DIRECTION.RIGHT);
                break;
            case 1:
                socket.sendDirection(DIRECTION.DOWN);
                break;
            case 2:
                socket.sendDirection(DIRECTION.LEFT);
                break;
            default:
                socket.sendDirection(DIRECTION.UP);
                break;
        }
        if (count > 100) {
            count = 0;
        }
    }

}
