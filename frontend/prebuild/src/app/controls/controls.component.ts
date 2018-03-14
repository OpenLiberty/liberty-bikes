import { Component, OnInit } from '@angular/core';
import { GameWebsocket } from '../net/websocket';
import { Triangle } from '../geom/triangle';
import { Point } from '../geom/point';

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

  upTriangle: Triangle;
  leftTriangle: Triangle;
  downTriangle: Triangle;
  rightTriangle: Triangle;

  constructor() {}

  ngOnInit() {
    this.initCanvas();

    this.initSocket();

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
  }

  initCanvas() {
    this.canvas = document.getElementById('dpad-canvas') as HTMLCanvasElement;
    this.context = this.canvas.getContext('2d');

    this.canvas.addEventListener('touchstart', (evt: TouchEvent) => {
      this.touchStarted(evt);

      // Prevent touch events and mouse events from doubling up.
      evt.preventDefault();
    });

    this.canvas.addEventListener('mousedown', (evt: MouseEvent) => {
      this.mouseDown(evt);
    });

    const canvasWidth = this.canvas.width;
    const canvasHeight = this.canvas.height;

    this.upTriangle = new Triangle(
      new Point(0, 0),
      new Point(canvasWidth, 0),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );

    this.leftTriangle = new Triangle(
      new Point(0, canvasHeight),
      new Point(0, 0),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );

    this.downTriangle = new Triangle(
      new Point(canvasWidth, canvasHeight),
      new Point(0, canvasHeight),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );

    this.rightTriangle = new Triangle(
      new Point(canvasWidth, 0),
      new Point(canvasWidth, canvasHeight),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );
  }

  initSocket() {
    this.roundId = sessionStorage.getItem('roundId');
    console.log(`Round ID: ${this.roundId}`);
    this.serverHost = document.location.hostname;
    this.serverPort = '8080';
    this.gameSocket = new GameWebsocket(
      this.serverHost,
      this.serverPort,
      this.roundId
    );

    this.gameSocket.openCallback = (evt: MessageEvent): any => {
      this.onConnect(evt);
    };

    this.gameSocket.messageCallback = (evt: MessageEvent): any => {
      this.onMessage(evt);
    };
  }

  onConnect(evt: MessageEvent) {
    const name = sessionStorage.getItem('username');
    this.gameSocket.sendText(JSON.stringify({ 'playerjoined': sessionStorage.getItem('userId'), 'hasGameBoard' : 'false'}));
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
    console.log(evt);
    if (evt.touches.length > 0) {
      this.canvasPressed(evt.touches[0].pageX, evt.touches[0].pageY);
    }
  }

  mouseDown(evt: MouseEvent) {
    console.log(evt);
    this.canvasPressed(evt.pageX, evt.pageY);
  }

  canvasPressed(x: number, y: number) {
    const locationX = (x - this.canvas.offsetLeft) * this.canvas.width / this.canvas.offsetWidth;
    const locationY = (y - this.canvas.offsetTop) * this.canvas.height / this.canvas.offsetHeight;

    const location = new Point(locationX, locationY);

    if (this.upTriangle.containsPoint(location)) {
      this.moveUp();
    }

    if (this.leftTriangle.containsPoint(location)) {
      this.moveLeft();
    }

    if (this.downTriangle.containsPoint(location)) {
      this.moveDown();
    }

    if (this.rightTriangle.containsPoint(location)) {
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
