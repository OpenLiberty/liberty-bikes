import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Meta } from '@angular/platform-browser';
import { GameService } from './game.service';
import { LoginComponent } from '../login/login.component';
import { EventSourcePolyfill } from 'ng-event-source';
import { environment } from './../../environments/environment';
import { Player } from '../entity/player';
import { Obstacle } from '../entity/obstacle';
import { PlayerTooltip } from '../entity/player.tooltip';
import { Shape, Stage, Shadow, Text, Ticker, Container, Tween, CSSPlugin, Ease } from 'createjs-module';
import { Constants } from './constants';
import { Card } from '../overlay/card';
import { bindCallback, timer, Observable, Subscription } from 'rxjs';

enum GameState {
  Waiting,
  Playing,
  Finished,
}

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss'],
  providers: [ GameService ],
})
export class GameComponent implements OnInit, OnDestroy {
  roundId: string;
  serverHost: string;
  serverPort: string;

  isSpectator: boolean;
  isSingleParty: boolean;

  partyId: string;
  showPartyId = false;
  showLoader = false;

  currentState: GameState;

  // Expose window object to template
  window = window;

  get state(): GameState {
    return this.currentState;
  }

  set state(newState: GameState) {
    this.currentState = newState;
    switch (newState) {
      case GameState.Waiting: {
        this.gameButtonText = 'Start Game';
        this.gameButtonDisabled = false;
        break;
      }
      case GameState.Playing: {
        this.gameButtonText = 'Start Game';

        // Firefox on Windows: focus gets stuck on the button if clicked,
        // and no keyboard events are sent to the game. Remove the
        // focus when the game starts.
        document.getElementById('gameButton').blur();

        this.gameButtonDisabled = true;
        break;
      }
      case GameState.Finished: {
        this.gameButtonText = 'Requeue';
        this.gameButtonDisabled = this.isSpectator;
        break;
      }

    }
  }

  gameButtonText: String;
  gameButtonDisabled = false;

  output: HTMLElement;
  idDisplay: HTMLElement;

  canvas: HTMLCanvasElement;
  context: CanvasRenderingContext2D;
  stage: Stage;

  overlayCanvas: HTMLCanvasElement;
  overlayContext: CanvasRenderingContext2D;
  overlayStage: Stage;

  waitCard: Card;
  waitingTimer: Observable<number>;
  waitingSub: Subscription;

  players: Map<string, Player> = new Map<string, Player>();
  obstacles: Obstacle[];
  trailsShape: Shape;
  trailsCanvas: HTMLCanvasElement;
  trailsContext: CanvasRenderingContext2D;
  obstaclesShape: Shape;
  obstaclePulseUp = true;

  constructor(private meta: Meta,
    private router: Router,
    private ngZone: NgZone,
    private http: HttpClient,
    private gameService: GameService,
  ) {
    this.ngZone.runOutsideAngular(() => {
      gameService.messages.subscribe((msg) => {
        const json = msg as any;
        if (json.errorMessage) {
          this.handleGameError(json.errorMessage);
        }
        if (json.countdown) {
          this.ngZone.run(() => this.startingCountdown(json.countdown));
        }
        if (json.awaitplayerscountdown) {
          console.log(`Got countdown value of ${json.awaitplayerscountdown}`);
          this.ngZone.run(() => this.waitForPlayers(json.awaitplayerscountdown));
        }
        if (json.keepAlive) {
          this.gameService.send({ keepAlive: true });
        }
        if (json.gameStatus === 'FINISHED') {
          this.finishGame();
        }
        if (json.obstacles) {
          this.updateObstacles(json.obstacles);
        }
        if (json.movingObstacles) {
          this.updateMovingObstacles(json.movingObstacles);
        }

        if (json.playerlist) {
          this.updatePlayerList(json.playerlist);
        }
        if (json.players) {
          this.updatePlayerStatus(json.players);
        }
        this.stage.update();
      }, (err) => {
        console.log(`Error occurred: ${err}`);
      });
    });
  }

  ngOnInit() {
    this.roundId = sessionStorage.getItem('roundId');

    this.isSpectator = sessionStorage.getItem('isSpectator') === 'true';
    this.isSingleParty = sessionStorage.getItem('isSingleParty') === 'true';

    if (this.isSpectator) {
      console.log('is a spectator... showing game id');
      // Set the Party ID and make visible
      this.partyId = sessionStorage.getItem('partyId');
      this.showPartyId = true;
      this.gameService.send({'spectatorjoined': true});
    } else {
      this.gameService.send({'playerjoined': sessionStorage.getItem('userId'), 'hasGameBoard' : 'true'});
    }

    this.meta.addTag({name: 'viewport', content: 'width=1600'}, true);

    this.output = document.getElementById('output');
    this.idDisplay = document.getElementById('gameIdDisplay');

    this.canvas = <HTMLCanvasElement> document.getElementById('gameCanvas');
    this.context = this.canvas.getContext('2d');
    this.stage = new Stage(this.canvas);

    this.overlayCanvas = <HTMLCanvasElement> document.getElementById('overlayCanvas');
    this.overlayContext = this.overlayCanvas.getContext('2d');
    this.overlayStage = new Stage(this.overlayCanvas);

    this.trailsShape = new Shape();
    this.trailsShape.x = 0;
    this.trailsShape.y = 0;

    this.stage.addChild(this.trailsShape);

    this.trailsCanvas = <HTMLCanvasElement> document.createElement('canvas');
    this.trailsContext = this.trailsCanvas.getContext('2d');
    this.trailsCanvas.width = Constants.BOARD_SIZE;
    this.trailsCanvas.height = Constants.BOARD_SIZE;

    this.obstaclesShape = new Shape();
    this.obstaclesShape.x = 0;
    this.obstaclesShape.y = 0;

    this.stage.addChild(this.obstaclesShape);

    // Create a red border around the game board
    const BORDER_WIDTH = 3;
    this.addBorder(0, 0, Constants.BOARD_SIZE, BORDER_WIDTH); // top
    this.addBorder(0, Constants.BOARD_SIZE - BORDER_WIDTH, Constants.BOARD_SIZE, BORDER_WIDTH); // bottom
    this.addBorder(0, 0, BORDER_WIDTH, Constants.BOARD_SIZE); // left
    this.addBorder(Constants.BOARD_SIZE - BORDER_WIDTH, 0, BORDER_WIDTH, Constants.BOARD_SIZE); // right

    this.stage.update();

    window.onkeydown = (e: KeyboardEvent): any => {
      const key = e.keyCode ? e.keyCode : e.which;

      if (key === 38 || key === 87) {
        this.moveUp();
      } else if (key === 40 || key === 83) {
        this.moveDown();
      } else if (key === 37 || key === 65) {
        this.moveLeft();
      } else if (key === 39 || key === 68) {
        this.moveRight();
      }
    };

    this.state = GameState.Waiting;
  }

  ngOnDestroy() {
    sessionStorage.removeItem('roundId');
    this.gameService.close();
  }

  // Event handlers
  handleGameError(errorMessage: any) {
    console.log('Received error message from server: ' + errorMessage);
    alert(errorMessage + ' Your connection to the game server has been closed. You will be redirected to the login page.');
    this.ngZone.run(() => {
      this.router.navigate(['/login']);
    });
  }

  finishGame() {
    console.log(`Round ${this.roundId} has finished.`);
    if (sessionStorage.getItem('isSpectator') === 'true') {
      this.requeue();
    }
  }

  updateObstacles(obstacles: any) {
    if (this.obstaclesShape.shadow) {
      // give that shadow a pulsating effect by oscillating the blur between 10-50
      let blur: number = this.obstaclesShape.shadow.blur;
      blur += this.obstaclePulseUp ? 2 : -2;
      if (blur >= 50) {
        this.obstaclePulseUp = false;
      }

      if (blur <= 10) {
        this.obstaclePulseUp = true;
      }

      this.obstaclesShape.shadow.blur = blur;
    } else {
      for (let obstacle of obstacles) {
        this.obstaclesShape.shadow = new Shadow(Obstacle.COLOR, 0, 0, 20);
        this.obstaclesShape.graphics.beginFill(Obstacle.COLOR).rect(
          obstacle.x * Constants.BOX_SIZE,
          obstacle.y * Constants.BOX_SIZE,
          obstacle.width * Constants.BOX_SIZE,
          obstacle.height * Constants.BOX_SIZE
        );
      }
    }
  }

  updateMovingObstacles(movingObstacles: any) {
    if (this.obstacles == null || this.obstacles.length !== movingObstacles.length) {
      // If we got obstacles for the first time, or number of obstacles changed, create new obstacles
      if (this.obstacles != null) {
        this.obstacles.forEach(obstacle => {
          if (obstacle.shape != null) {
            this.stage.removeChild(obstacle.shape);
          }
        });
      }
      this.obstacles = new Array<Obstacle>();
      movingObstacles.forEach((obstacle, i) => {
        let newObstacle: Obstacle = new Obstacle(obstacle.width * Constants.BOX_SIZE, obstacle.height * Constants.BOX_SIZE);
        newObstacle.update(obstacle.x * Constants.BOX_SIZE, obstacle.y * Constants.BOX_SIZE);
        this.obstacles.push(newObstacle);
        this.stage.addChild(newObstacle.shape);
      });
    } else {
      // Otherwise, just update the shape positions
      movingObstacles.forEach((obstacle, i) => {
        this.obstacles[i].update(obstacle.x * Constants.BOX_SIZE, obstacle.y * Constants.BOX_SIZE);
        this.trailsContext.clearRect(
          obstacle.x * Constants.BOX_SIZE,
          obstacle.y * Constants.BOX_SIZE,
          obstacle.width * Constants.BOX_SIZE,
          obstacle.height * Constants.BOX_SIZE
        );
      });
    }
  }

  updatePlayerList(playerlist: any) {
    let oldPlayers: Map<string, Player> = new Map<string, Player>(this.players);
    this.players.clear();
    for (let playerInfo of playerlist) {
      // Don't add generic bot players (used to pad out the playerlist)
      if (playerInfo.id === '') {
        continue;
      }

      let newPlayer = oldPlayers.get(playerInfo.id);

      if (!newPlayer) {
        newPlayer = new Player();
        newPlayer.name = playerInfo.name;
        newPlayer.color = playerInfo.color;
        newPlayer.status = playerInfo.status;
      } else {
        oldPlayers.delete(playerInfo.id);
      }

      this.players.set(playerInfo.id, newPlayer);

      newPlayer.update(
        playerInfo.x * Constants.BOX_SIZE + (playerInfo.width / 2) * Constants.BOX_SIZE,
        playerInfo.y * Constants.BOX_SIZE + (playerInfo.width / 2) * Constants.BOX_SIZE,
        playerInfo.direction
      );

      this.stage.addChild(newPlayer.object);
    }

    oldPlayers.forEach((playerThatLeft: Player, id: string) => {
      this.stage.removeChild(playerThatLeft.object);
    });
  }

  updatePlayerStatus(players: any) {
    let noneAlive = true;
    let playersMoved = false;
    players.forEach((player, i) => {
      const playerEntity = this.players.get(player.id);
      if (player.status === 'Alive') {
        noneAlive = false;

        if (playerEntity.update(
          player.x * Constants.BOX_SIZE + (player.width / 2) * Constants.BOX_SIZE,
          player.y * Constants.BOX_SIZE + (player.height / 2) * Constants.BOX_SIZE,
          player.direction)
        ) {
          playersMoved = true;
        }
        // Stamp down player on trails canvas so it can be erased properly when obstacles roll over it
        this.trailsContext.shadowBlur = 20;
        this.trailsContext.shadowColor = player.color;
        this.trailsContext.fillStyle = player.color;
        this.trailsContext.fillRect(
          Constants.BOX_SIZE * player.x + (player.width / 2) * Constants.BOX_SIZE - Constants.BOX_SIZE / 2,
          Constants.BOX_SIZE * player.y + (player.height / 2) * Constants.BOX_SIZE - Constants.BOX_SIZE / 2,
          Constants.BOX_SIZE,
          Constants.BOX_SIZE
        );
      } else if (!player.alive) {
        // Ensure tooltip is hidden in case player dies before it fades out
        playerEntity.tooltip.visible(false);
        playerEntity.tooltip.alpha = 1;

        if (this.state !== GameState.Finished && player.id === sessionStorage.getItem('userId')) {
          this.ngZone.run(() => this.state = GameState.Finished);
        }
      }

      playerEntity.setStatus(player.status);
    });

    if (playersMoved) {
      this.trailsShape.graphics.clear()
        .beginBitmapFill(this.trailsCanvas, 'no-repeat')
        .drawRect(0, 0, Constants.BOARD_SIZE, Constants.BOARD_SIZE);
    }

    if (noneAlive) {
      if (this.state === GameState.Playing) {
        if (this.isSpectator) {
          this.countdownRequeue(5);
        } else {
          this.state = GameState.Finished;
        }
      }

      this.players.forEach((player: Player, id: string) => {
        player.tooltip.alpha = 1;
        player.tooltip.visible(true);

        if (player.status.toLocaleLowerCase() === 'winner') {
          const winnerCard = new Card(400, 'winner!', player.name, true, player.color);
          const card = winnerCard.displayObject;
          card.x = (Constants.BOARD_SIZE / 2) - (winnerCard.width / 2);
          card.y = (Constants.BOARD_SIZE / 2) - (winnerCard.height / 2);

          this.overlayStage.removeAllChildren();
          this.overlayStage.addChild(card);
          this.overlayStage.update();

          Ticker.on('tick', (evt) => this.updateOverlay(evt));
          Ticker.framerate = 60;
          winnerCard.show();
        }
      });
    }
  }

  // Game actions
  gameButtonPressed() {
    switch (this.state) {
      case GameState.Waiting: {
        this.startGame();
        break;
      }
      case GameState.Finished: {
        this.requeue();
        break;
      }
    }
  }

  startGame() {
    this.verifyOpen();
    this.gameService.send({ message: 'GAME_START' });
  }

  async requeue() {
    if (!this.isSpectator) {
      this.gameButtonDisabled = true;
      this.gameButtonText = 'Requeuing...';
    }
    let partyId = sessionStorage.getItem('partyId');
    let isSpectator: boolean = sessionStorage.getItem('isSpectator') === 'true' ? true : false;
    if (isSpectator || partyId === null) {
      let roundId: string = sessionStorage.getItem('roundId');
      let nextRoundID: any = await this.http.get(
        `${environment.API_URL_GAME_ROUND}/${roundId}/requeue?isPlayer=${!isSpectator}`,
        { responseType: 'text' }
      ).toPromise();
      // if a spectator, wait 5s before moving to next round to let people look at the final state of the board a bit
      if (isSpectator) {
        console.log(`Will requeue to round ${nextRoundID} in 5 seconds.`);
        setTimeout(() => {
          this.processRequeue(nextRoundID);
        }, 5000);
      } else {
        this.processRequeue(nextRoundID);
      }
    } else {
      let playerId = sessionStorage.getItem('userId');
      let queueCallback = new EventSourcePolyfill(`${environment.API_URL_PARTY}/${partyId}/queue?playerId=${playerId}`, {});
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
      };
      queueCallback.onerror = msg => {
        console.log('Error showing queue position: ' + JSON.stringify(msg.data));
      };
    }
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

  processRequeue(newRoundId) {
    console.log(`Requeueing to round ${newRoundId}`);
    this.roundId = newRoundId;
    sessionStorage.setItem('roundId', this.roundId);
    location.reload();
  }

  startingCountdown(seconds) {
    this.state = GameState.Playing;
    this.waitingSub.unsubscribe();

    this.waitCard.headerString = 'Get Ready';
    this.waitCard.bodyString = `${seconds}`;
    this.waitCard.emphasizeBody = true;

    const card = this.waitCard.displayObject;

    const scaleFactor = 1.5;

    Tween.get(card).to({
      scaleX: scaleFactor,
      scaleY: scaleFactor,
      x: (Constants.BOARD_SIZE / 2) - ((this.waitCard.width * scaleFactor) / 2),
      y: (Constants.BOARD_SIZE / 2) - ((this.waitCard.height * scaleFactor) / 2)
    }, 100);

    this.waitingTimer = timer(0, 1000);
    this.waitingSub = this.waitingTimer.subscribe((t) => {
      if (t < seconds - 1) {
        this.waitCard.bodyString = `${seconds - (t + 1)}`;
      } else if (t === seconds - 1) {
        this.waitCard.bodyString = 'GO!';
        this.waitCard.hide(500);
      } else if (t >= seconds) {
        this.ngZone.run(() => {
          this.waitingSub.unsubscribe();
        });
      }
    });
  }

  countdownRequeue(seconds) {
    if (this.waitingSub) {
      this.waitingSub.unsubscribe();
    }
    this.waitingTimer = timer(0, 1000);
    this.waitingSub = this.waitingTimer.subscribe((t) => {
      if (seconds < 1) {
        this.ngZone.run(() => {
          this.gameButtonText = `Requeuing...`;
        });

        this.waitingSub.unsubscribe();
      } else {
        this.ngZone.run(() => {
          this.gameButtonText = `Requeue in ${seconds}...`;
        });
      }
      seconds = seconds - 1;
    });
  }

  waitForPlayers(seconds) {
    this.state = GameState.Waiting;
    if (this.waitingSub) {
      this.waitingSub.unsubscribe();
    }
    this.waitingTimer = timer(0, 1000);
    this.waitingSub = this.waitingTimer.subscribe((t) => {
      if (this.waitCard) {
        this.waitCard.bodyString = `${seconds - t}`;
        // If we have gotten down to -2, a connection was probably interrupted
        if (-1 > (seconds - t)) {
          this.waitingSub.unsubscribe();
          this.connectionInterrupted();
        }
      }
    });

    if (!this.waitCard) {
      const width = 400;
      const margin = 50;
      this.waitCard = new Card(width, 'waiting for players', `${seconds}`, true);
      const card = this.waitCard.displayObject;
      card.x = (Constants.BOARD_SIZE / 2) - (this.waitCard.width / 2);
      card.y = (Constants.BOARD_SIZE / 2) - (this.waitCard.height / 2);

      this.overlayStage.addChild(card);
      this.overlayStage.update();

      Ticker.on('tick', (evt) => this.updateOverlay(evt));
      Ticker.framerate = 60;
      this.waitCard.show();

    } else {
      this.waitCard.bodyString = `${seconds}`;
    }
  }

  updateOverlay(event) {
    this.overlayStage.update(event);
  }

  verifyOpen() {
    if (!this.gameService.isOpen()) {
      this.connectionInterrupted();
    }
  }

  connectionInterrupted() {
    alert('Connection to game-service interrrupted. Re-directing to login page.');
    this.ngZone.run(() => {
      this.router.navigate(['/login']);
    });
  }

  addBorder(x, y, w, h) {
    let shape: Shape = new Shape();
    shape.shadow = new Shadow(Obstacle.COLOR, 0, 0, 20);
    shape.graphics.beginFill(Obstacle.COLOR).rect(x, y, w, h);
    this.stage.addChild(shape);
  }

}
