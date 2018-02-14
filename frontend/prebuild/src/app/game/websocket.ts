import * as $ from 'jquery';
import { Whiteboard } from './whiteboard';

export class GameWebsocket {
  roundId: string;
  baseUri: string;
  wsUri: string;
  websocket: WebSocket;
  whiteboard: Whiteboard;
  output: HTMLElement;

  constructor(whiteboard: Whiteboard) {
    this.roundId = sessionStorage.getItem('roundId');
    this.baseUri = `ws://${document.location.hostname}:8080/round/ws`;
    this.wsUri = `${this.baseUri}/${this.roundId}`;
    this.websocket = new WebSocket(this.wsUri);
    this.whiteboard = whiteboard;
    this.output = document.getElementById('output');

    this.websocket.onmessage = (evt: MessageEvent): any => {
      this.onMessage(evt);
    };

    this.websocket.onerror = (evt: MessageEvent): any => {
      this.onError(evt);
    };

    this.websocket.onopen = (evt: MessageEvent): any => {
      this.onConnect(evt);
    };
  }

  sendText(json: string) {
    console.log(`sending text: ${json}`);
    this.websocket.send(json);
  }

  onMessage(evt: MessageEvent) {
    console.log(`received: ${evt.data}`);
    if (typeof evt.data === 'string') {
      const json = JSON.parse(evt.data);
      if (json.playerlist) {
        this.whiteboard.updatePlayerList(json);
      }
      if (json.requeue) {
        this.roundId = json.requeue;
        sessionStorage.setItem('roundId', this.roundId);
        location.reload();
      }
      if (json.obstacles) {
        for (let obstacle of json.obstacles) {
          this.whiteboard.drawObstacle(obstacle);
        }
      }
      if (json.players) {
        for (let player of json.players) {
          this.whiteboard.drawPlayer(player);
        }
      }
    }
  }

  onError(evt: MessageEvent) {
    this.writeToScreen(`<span style="color: red;">ERROR:</span> ${evt.data}`);
  }

  onConnect(evt: MessageEvent) {
    const name = sessionStorage.getItem('username');
    const isSpectator = sessionStorage.getItem('isSpectator');
    if (isSpectator === 'true') {
      console.log('is a spectator... showing game id');
      // Set the Round ID and make visible
      const gameId = $('#gameIdDisplay');
      gameId.html('Round ID: ' + this.roundId);
      gameId.removeClass('d-none');
      gameId.addClass('d-inline-block');
      this.sendText(JSON.stringify({'spectatorjoined': true}));
    } else {
      this.sendText(JSON.stringify({'playerjoined': name}));
    }
  }

  writeToScreen(message: string) {
    const pre = document.createElement('p');
    pre.style.wordWrap = 'break-word';
    pre.innerHTML = message;
    this.output.appendChild(pre);
  }
}
