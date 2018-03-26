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

    this.canvas.addEventListener('touchend', (evt:TouchEvent) => {
      this.touchEnded(evt);
    });

    this.canvas.addEventListener('mousedown', (evt: MouseEvent) => {
      this.mouseDown(evt);
    });

    this.canvas.addEventListener('mouseup', (evt: MouseEvent) => {
      this.mouseUp(evt);
    });

    const canvasWidth = this.canvas.width;
    const canvasHeight = this.canvas.height;

    this.upTriangle = new Triangle(
      new Point(0 + 1, 0 + 1),
      new Point(canvasWidth - 1, 0 + 1),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );

    this.leftTriangle = new Triangle(
      new Point(0 + 1, canvasHeight - 1),
      new Point(0 + 1, 0 + 1),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );

    this.downTriangle = new Triangle(
      new Point(canvasWidth - 1, canvasHeight - 1),
      new Point(0 + 1, canvasHeight - 1),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );

    this.rightTriangle = new Triangle(
      new Point(canvasWidth - 1, 0 + 1),
      new Point(canvasWidth - 1, canvasHeight - 1),
      new Point(canvasWidth / 2, canvasHeight / 2)
    );

    window.requestAnimationFrame(() => this.draw());
  }

  draw() {
    const ctx = this.context;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    ctx.strokeStyle = 'white';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';

    ctx.lineWidth = 2;


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

    // Draw up arrow
    const upArrow = this.upTriangle.scale(0.3).rotate(Math.PI);
    ctx.beginPath();
    ctx.moveTo(upArrow.point1.x, upArrow.point1.y);
    ctx.lineTo(upArrow.point2.x, upArrow.point2.y);
    ctx.lineTo(upArrow.point3.x, upArrow.point3.y);
    ctx.closePath();
    ctx.stroke();

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

    // Draw left arrow
    const leftArrow = this.leftTriangle.scale(0.3).rotate(Math.PI);
    ctx.beginPath();
    ctx.moveTo(leftArrow.point1.x, leftArrow.point1.y);
    ctx.lineTo(leftArrow.point2.x, leftArrow.point2.y);
    ctx.lineTo(leftArrow.point3.x, leftArrow.point3.y);
    ctx.closePath();
    ctx.stroke();

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

    // Draw down arrow
    const downArrow = this.downTriangle.scale(0.3).rotate(Math.PI);
    ctx.beginPath();
    ctx.moveTo(downArrow.point1.x, downArrow.point1.y);
    ctx.lineTo(downArrow.point2.x, downArrow.point2.y);
    ctx.lineTo(downArrow.point3.x, downArrow.point3.y);
    ctx.closePath();
    ctx.stroke();

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

    // Draw right arrow
    const rightArrow = this.rightTriangle.scale(0.3).rotate(Math.PI);
    ctx.beginPath();
    ctx.moveTo(rightArrow.point1.x, rightArrow.point1.y);
    ctx.lineTo(rightArrow.point2.x, rightArrow.point2.y);
    ctx.lineTo(rightArrow.point3.x, rightArrow.point3.y);
    ctx.closePath();
    ctx.stroke();

    window.requestAnimationFrame(() => this.draw());
  }

  touchStarted(evt: TouchEvent) {
    console.log(evt);
    if (evt.touches.length > 0) {
      this.canvasPressed(evt.touches[0].pageX, evt.touches[0].pageY);
    }
  }

  touchEnded(evt: TouchEvent) {
    console.log(evt);
    this.canvasReleased(evt.changedTouches[0].pageX, evt.changedTouches[0].pageY);
  }

  mouseDown(evt: MouseEvent) {
    console.log(evt);
    this.canvasPressed(evt.pageX, evt.pageY);
  }

  mouseUp(evt: MouseEvent) {
    console.log(evt);
    this.canvasReleased(evt.pageX, evt.pageY);
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

  canvasReleased(x: number, y: number) {
    const locationX = (x - this.canvas.offsetLeft) * this.canvas.width / this.canvas.offsetWidth;
    const locationY = (y - this.canvas.offsetTop) * this.canvas.height / this.canvas.offsetHeight;

    const location = new Point(locationX, locationY);

    if (this.upTriangle.containsPoint(location)) {
      this.upPressed = false;
    }

    if (this.leftTriangle.containsPoint(location)) {
      this.leftPressed = false;
    }

    if (this.downTriangle.containsPoint(location)) {
      this.downPressed = false;
    }

    if (this.rightTriangle.containsPoint(location)) {
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
