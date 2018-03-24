import { Component, OnInit } from '@angular/core';
import { GameService } from '../game/game.service';
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

  upTriangle: Triangle;
  leftTriangle: Triangle;
  downTriangle: Triangle;
  rightTriangle: Triangle;

  upPressed: boolean;
  leftPressed: boolean;
  downPressed: boolean;
  rightPressed: boolean;

  constructor(private gameService: GameService) {
    gameService.messages.subscribe((msg) => {
      const json = msg as any;
      console.log(`received: ${JSON.stringify(json)}`);
      if (json.requeue) {
        this.roundId = json.requeue;
        sessionStorage.setItem('roundId', this.roundId);
        location.reload();
      }
    }, (err) => {
      console.log(err);
    });
  }

  ngOnInit() {
    const name = sessionStorage.getItem('username');
    this.gameService.send({ 'playerjoined': sessionStorage.getItem('userId'), 'hasGameBoard' : 'false'});

    this.initCanvas();

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

    window.requestAnimationFrame(() => this.draw());
  }

  draw() {
    const ctx = this.context;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    ctx.strokeStyle = 'white';
    ctx.fillStyle = 'white';


    // Draw up button
    ctx.beginPath();
    ctx.moveTo(this.upTriangle.point1.x, this.upTriangle.point1.y);
    ctx.lineTo(this.upTriangle.point2.x, this.upTriangle.point2.y);
    ctx.lineTo(this.upTriangle.point3.x, this.upTriangle.point3.y);
    ctx.closePath();
    ctx.stroke();

    if (this.upPressed) {
      ctx.fill();
    }

    // Draw left button
    ctx.beginPath();
    ctx.moveTo(this.leftTriangle.point1.x, this.leftTriangle.point1.y);
    ctx.lineTo(this.leftTriangle.point2.x, this.leftTriangle.point2.y);
    ctx.lineTo(this.leftTriangle.point3.x, this.leftTriangle.point3.y);
    ctx.closePath();
    ctx.stroke();

    if (this.leftPressed) {
      ctx.fill();
    }

    // Draw down button
    ctx.beginPath();
    ctx.moveTo(this.downTriangle.point1.x, this.downTriangle.point1.y);
    ctx.lineTo(this.downTriangle.point2.x, this.downTriangle.point2.y);
    ctx.lineTo(this.downTriangle.point3.x, this.downTriangle.point3.y);
    ctx.closePath();
    ctx.stroke();

    if (this.downPressed) {
      ctx.fill();
    }

    // Draw right button
    ctx.beginPath();
    ctx.moveTo(this.rightTriangle.point1.x, this.rightTriangle.point1.y);
    ctx.lineTo(this.rightTriangle.point2.x, this.rightTriangle.point2.y);
    ctx.lineTo(this.rightTriangle.point3.x, this.rightTriangle.point3.y);
    ctx.closePath();
    ctx.stroke();

    if (this.rightPressed) {
      ctx.fill();
    }

    window.requestAnimationFrame(() => this.draw());
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
      this.upPressed = true;
      this.moveUp();
    } else {
      this.upPressed = false;
    }

    if (this.leftTriangle.containsPoint(location)) {
      this.leftPressed = true;
      this.moveLeft();
    } else {
      this.leftPressed = false;
    }

    if (this.downTriangle.containsPoint(location)) {
      this.downPressed = true;
      this.moveDown();
    } else {
      this.downPressed = false;
    }

    if (this.rightTriangle.containsPoint(location)) {
      this.rightPressed = true;
      this.moveRight();
    } else {
      this.rightPressed = false;
    }
  }

  // Game actions
  startGame() {
    this.gameService.send({ message: 'GAME_START' });
  }

  requeue() {
    this.gameService.send({ message: 'GAME_REQUEUE' });
  }

  moveUp() {
    this.gameService.send({ direction: 'UP' });
  }

  moveDown() {
    this.gameService.send({ direction: 'DOWN' });
  }

  moveLeft() {
    this.gameService.send({ direction: 'LEFT' });
  }

  moveRight() {
    this.gameService.send({ direction: 'RIGHT' });
  }
}
