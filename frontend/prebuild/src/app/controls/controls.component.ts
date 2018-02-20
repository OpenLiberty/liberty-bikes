import { Component, OnInit } from '@angular/core';
import { GameWebsocket } from '../net/websocket';

@Component({
  selector: 'app-controls',
  templateUrl: './controls.component.html',
  styleUrls: ['./controls.component.scss']
})
export class ControlsComponent implements OnInit {
  roundId: string;
  serverHost: string;
  serverPort: string;

  gameSocket: GameWebsocket;

  constructor() {  }

  ngOnInit() {
    this.roundId = sessionStorage.getItem('roundId');
    console.log(`Round ID: ${this.roundId}`);
    this.serverHost = document.location.hostname;
    this.serverPort = '8080';
    this.gameSocket = new GameWebsocket(this.serverHost, this.serverPort, this.roundId);

    window.onkeydown = (e: KeyboardEvent): any => {
      const key = e.keyCode ? e.keyCode : e.which;

      if (key === 38) {
        this.moveUp();
      } else if (key === 40) {
        this.moveDown();
      } else if (key === 37) {
        this.moveLeft();
      } else if (key === 39) {
        this.moveRight();
      }
    };

    this.gameSocket.openCallback = (evt: MessageEvent): any => {
      this.onConnect(evt);
    };

    this.gameSocket.messageCallback = (evt: MessageEvent): any => {
      this.onMessage(evt);
    };
  }

  onConnect(evt: MessageEvent) {
    const name = sessionStorage.getItem('username');
    this.gameSocket.sendText(JSON.stringify({'playerjoined': name}));
  }

  onMessage(evt: MessageEvent) {
    console.log(`received: ${evt.data}`);
    if (typeof evt.data === 'string') {
      const json = JSON.parse(evt.data);
      if (json.requeue) {
        this.roundId = json.requeue;
        sessionStorage.setItem('roundId', this.roundId);
        location.reload();
      }
    }
  }


  moveUp() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'UP' }));
  }

  moveDown() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'DOWN' }));
  }

  moveLeft() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'LEFT' }));
  }

  moveRight() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'RIGHT' }));
  }

}
