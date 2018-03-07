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

  canvas: HTMLCanvasElement;
  context: CanvasRenderingContext2D;

  gameSocket: GameWebsocket;

  constructor() {}

  ngOnInit() {
    this.canvas = document.getElementById('dpad-canvas') as HTMLCanvasElement;
    this.context = this.canvas.getContext('2d');

    this.canvas.addEventListener("touchstart", (evt: TouchEvent) => {
      this.touchStarted(evt);
    });

    this.canvas.addEventListener("mousedown", (evt: MouseEvent) => {
      this.mouseDown(evt);
    })

    this.roundId = sessionStorage.getItem('roundId');
    console.log(`Round ID: ${this.roundId}`);
    this.serverHost = document.location.hostname;
    this.serverPort = '8080';
    this.gameSocket = new GameWebsocket(
      this.serverHost,
      this.serverPort,
      this.roundId
    );

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
    this.gameSocket.sendText(JSON.stringify({ playerjoined: name }));
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

  touchStarted(evt: TouchEvent) {
    if (evt.touches.length > 0) {
      this.canvasPressed(evt.touches[0].pageX, evt.touches[0].pageY);
    }
  }

  mouseDown(evt: MouseEvent) {
    this.canvasPressed(evt.pageX, evt.pageY);
  }

  canvasPressed(x: number, y: number) {
    const locationX = ((x - this.canvas.offsetLeft) * this.canvas.width) / this.canvas.offsetWidth;
    const locationY = ((y - this.canvas.offsetTop) * this.canvas.height) / this.canvas.offsetHeight;


    if (locationX >= 300 && locationX < 500 && locationY >= 0 && locationY < 300) {
      this.moveUp();
    }

    if (locationX >= 0 && locationX < 300 && locationY >= 300 && locationY < 500) {
      this.moveLeft();
    }

    if (locationX >= 300 && locationX < 500 && locationY >= 500 && locationY < 800) {
      this.moveDown();
    }

    if (locationX >= 500 && locationX < 800 && locationY >= 300 && locationY < 500) {
      this.moveRight();
    }
  }

  // Game actions
  startGame() {
    this.gameSocket.sendText(JSON.stringify({ message: 'GAME_START' }));
  }

  requeue() {
    this.gameSocket.sendText(JSON.stringify({ message: 'GAME_REQUEUE' }));
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
