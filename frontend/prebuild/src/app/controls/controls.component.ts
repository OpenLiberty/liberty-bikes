import { Component, NgZone, OnInit, OnDestroy } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { GameService } from '../game/game.service';
import { Triangle } from '../geom/triangle';
import { Point } from '../geom/point';
import { LoginComponent } from '../login/login.component';
import * as EventSource from 'eventsource';
import { environment } from './../../environments/environment';

@Component({
  selector: 'app-controls',
  templateUrl: './controls.component.html',
  styleUrls: ['./controls.component.scss'],
  providers: [ GameService ],
})
export class ControlsComponent implements OnInit, OnDestroy {
  windowHeight = window.innerHeight;

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
  
  currentDirection: string;

  private preventScrolling = (evt: TouchEvent) => {
    evt.preventDefault();
  }

  private pageWasResized = (evt: DeviceOrientationEvent) => {
    this.windowHeight = window.innerHeight;
    window.scrollTo(0, 0);
  }

  constructor(private router: Router,
		      private ngZone: NgZone,
		      private http: HttpClient,
		      private gameService: GameService) {
    gameService.messages.subscribe((msg) => {
      const json = msg as any;
      console.log(`received: ${JSON.stringify(json)}`);
      if (json.errorMessage) {
        console.log('Received error message from server: ' + json.errorMessage);
        alert('Your connection to the game server has been closed. You will be redirected to the login page.');
        this.ngZone.run(() => {
          this.router.navigate(['/login']);
        });
      }
      if (json.keepAlive) {
        this.gameService.send({ keepAlive: true });
      }
      if (json.gameStatus === 'FINISHED') {
    	    if (confirm('Game is over, requeue to next round?')) {
    	    	  this.requeue();
    	    } else {
          this.ngZone.run(() => {
            this.router.navigate(['/login']);
          });
    	    }
      }
    }, (err) => {
      console.log(err);
    });
  }

  ngOnInit() {
    const name = sessionStorage.getItem('username');
    this.gameService.send({ 'playerjoined': sessionStorage.getItem('userId'), 'hasGameBoard' : 'false'});

    this.initCanvas();

    // Make sure the view is at the top of the page so touch event coordinates line up
    window.scrollTo(0, 0);
    window.addEventListener('orientationchange', this.pageWasResized);
    window.addEventListener('resize', this.pageWasResized);
  }

  initCanvas() {
    window.addEventListener('touchmove', this.preventScrolling);
    this.canvas = document.getElementById('dpad-canvas') as HTMLCanvasElement;
    this.context = this.canvas.getContext('2d');

    this.canvas.addEventListener('touchstart', (evt: TouchEvent) => {
      this.touchStarted(evt);

      // Prevent touch events and mouse events from doubling up.
      evt.preventDefault();
    });

    this.canvas.addEventListener('touchmove', (evt: TouchEvent) => {
      this.touchMoved(evt);

      // Prevent touch events and mouse events from doubling up.
      evt.preventDefault();
    });

    this.canvas.addEventListener('touchend', (evt: TouchEvent) => {
      this.touchEnded(evt);

      // Prevent touch events and mouse events from doubling up.
      evt.preventDefault();
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
  
  processRequeue(newRoundId) {
    console.log(`Requeueing to round ${newRoundId}`);
    this.roundId = newRoundId;
    sessionStorage.setItem('roundId', this.roundId);
    location.reload();
  }

  draw() {
    if (window.scrollY !== 0) {
      window.scrollTo(0, 0);
    }
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
  }

  touchStarted(evt: TouchEvent) {
    if (evt.touches.length > 0) {
      this.canvasPressed(evt.touches[0].pageX, evt.touches[0].pageY);
    }
  }

  touchMoved(evt: TouchEvent) {
    if (evt.changedTouches.length > 0) {
      this.canvasPressed(evt.changedTouches[0].pageX, evt.changedTouches[0].pageY);
    }
  }

  touchEnded(evt: TouchEvent) {
    this.canvasReleased(evt.changedTouches[0].pageX, evt.changedTouches[0].pageY);
    this.verifyOpen();
  }

  mouseDown(evt: MouseEvent) {
    this.canvasPressed(evt.pageX, evt.pageY);
  }

  mouseUp(evt: MouseEvent) {
    this.canvasReleased(evt.pageX, evt.pageY);
    this.verifyOpen();
  }

  canvasPressed(x: number, y: number) {
    const locationX = (x - this.canvas.offsetLeft) * this.canvas.width / this.canvas.offsetWidth;
    const locationY = (y - this.canvas.offsetTop) * this.canvas.height / this.canvas.offsetHeight;

    const location = new Point(locationX, locationY);

    if (this.upTriangle.containsPoint(location)) {
      this.upPressed = true;
      this.setDirection('UP');
    } else {
      this.upPressed = false;
    }

    if (this.leftTriangle.containsPoint(location)) {
      this.leftPressed = true;
      this.setDirection('LEFT');
    } else {
      this.leftPressed = false;
    }

    if (this.downTriangle.containsPoint(location)) {
      this.downPressed = true;
      this.setDirection('DOWN');
    } else {
      this.downPressed = false;
    }

    if (this.rightTriangle.containsPoint(location)) {
      this.rightPressed = true;
      this.setDirection('RIGHT');
    } else {
      this.rightPressed = false;
    }

    window.requestAnimationFrame(() => this.draw());
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

    window.requestAnimationFrame(() => this.draw());
    this.verifyOpen();
  }

  // Game actions
  async requeue() {
    let partyId: string = sessionStorage.getItem('partyId');
    if (partyId === null) {
        let roundId: string = sessionStorage.getItem('roundId');
        let nextRoundID: any = await this.http.get(`${environment.API_URL_GAME_ROUND}/${roundId}/requeue?isPlayer=true`, { responseType: 'text' }).toPromise();
        this.processRequeue(nextRoundID);
    } else {
      let queueCallback = new EventSource(`${environment.API_URL_PARTY}/${partyId}/queue`);
      queueCallback.onmessage = msg => {
        let queueMsg = JSON.parse(msg.data);
        if (queueMsg.queuePosition) {
          // go to login page, reuse the same EventSource
          LoginComponent.queueCallback = queueCallback;
          sessionStorage.setItem('queuePosition', queueMsg.queuePosition);
          this.ngZone.run(() => {
            this.router.navigate(['/login']);
          });
        } else if (queueMsg.requeue) {
          console.log(`ready to join game! Joining round ${queueMsg.requeue}`);
          queueCallback.close();
          this.processRequeue(queueMsg.requeue);
        } else {
          console.log('Error: unrecognized message ' + msg.data);
        }
      }
      queueCallback.onerror = msg => {
        console.log('Error showing queue position: ' + JSON.stringify(msg.data));
      }
    }
  }

  setDirection(newDir: string) {
    if (this.currentDirection !== null && this.currentDirection !== newDir) {
      this.currentDirection = newDir;
      this.gameService.send({ direction: `${newDir}` });
    }
  }
  
  verifyOpen() {
    if (!this.gameService.isOpen()) {
    	  console.log('GameService socket not open');
    	  this.ngZone.run(() => {
        this.router.navigate(['/login']);
      });
    }
  }

  ngOnDestroy() {
    window.removeEventListener('touchmove', this.preventScrolling);
    window.removeEventListener('orientationchange', this.pageWasResized);
    window.removeEventListener('resize', this.pageWasResized);
    sessionStorage.removeItem('roundId');
    this.gameService.close();
  }
}
