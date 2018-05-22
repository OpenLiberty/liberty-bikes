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
import { Shape, Stage, Text } from 'createjs-module';
import { Constants } from './constants';

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

  partyId: string;
  showPartyId = false;
  showLoader = false;

  output: HTMLElement;
  idDisplay: HTMLElement;

  canvas: HTMLCanvasElement;
  context: CanvasRenderingContext2D;
  stage: Stage;

  players: Map<string,Player> = new Map<string,Player>();
  obstacles: Obstacle[];
  trailsShape: Shape;
  trailsCanvas: HTMLCanvasElement;
  trailsContext: CanvasRenderingContext2D;
  obstaclesShape: Shape;

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

    if (sessionStorage.getItem('isSpectator') === 'true') {
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

    this.stage.update();

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

  ngOnDestroy() {
    sessionStorage.removeItem('roundId');
    this.gameService.close();
  }

  // Event handlers
  handleGameError(errorMessage: any) {
    console.log('Received error message from server: ' + errorMessage);
    alert('Your connection to the game server has been closed. You will be redirected to the login page.');
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
    for (let obstacle of obstacles) {
      this.obstaclesShape.graphics.beginFill(Obstacle.COLOR).rect(obstacle.x * Constants.BOX_SIZE, obstacle.y * Constants.BOX_SIZE, obstacle.width * Constants.BOX_SIZE, obstacle.height * Constants.BOX_SIZE);
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
        this.trailsContext.clearRect(obstacle.x * Constants.BOX_SIZE, obstacle.y * Constants.BOX_SIZE, obstacle.width * Constants.BOX_SIZE, obstacle.height * Constants.BOX_SIZE);
      });
    }
  }

  updatePlayerList(playerlist: any) {
    let oldPlayers: Map<string,Player> = new Map<string,Player>(this.players);
    this.players.clear();
    for (let playerInfo of playerlist) {
      // Don't add generic bot players (used to pad out the playerlist)
      if (playerInfo.id === "")
        continue;

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

      newPlayer.update(playerInfo.x * Constants.BOX_SIZE + (playerInfo.width / 2) * Constants.BOX_SIZE, playerInfo.y * Constants.BOX_SIZE + (playerInfo.width / 2) * Constants.BOX_SIZE, playerInfo.direction);

      this.stage.addChild(newPlayer.object);
    }

    oldPlayers.forEach((playerThatLeft: Player, id: string) => {
      this.stage.removeChild(playerThatLeft.object);
    });
  }

  updatePlayerStatus(players: any) {
    let noneAlive: boolean = true;
    let playersMoved: boolean = false;
    players.forEach((player, i) => {
      const playerEntity = this.players.get(player.id);
      if (player.status === 'Alive') {
        noneAlive = false;

        if (playerEntity.update(player.x * Constants.BOX_SIZE + (player.width / 2) * Constants.BOX_SIZE, player.y * Constants.BOX_SIZE + (player.height / 2) * Constants.BOX_SIZE, player.direction)) {
          playersMoved = true;
        }
        // Stamp down player on trails canvas so it can be erased properly when obstacles roll over it
        this.trailsContext.shadowBlur = 20;
        this.trailsContext.shadowColor = player.color;
        this.trailsContext.fillStyle = player.color;
        this.trailsContext.fillRect(Constants.BOX_SIZE * player.x + (player.width / 2) * Constants.BOX_SIZE - Constants.BOX_SIZE / 2, Constants.BOX_SIZE * player.y + (player.height / 2) * Constants.BOX_SIZE - Constants.BOX_SIZE / 2, Constants.BOX_SIZE, Constants.BOX_SIZE);
      } else if (!player.alive) {
        // Ensure tooltip is hidden in case player dies before it fades out
        playerEntity.tooltip.visible(false);
        playerEntity.tooltip.alpha = 1;
      }

      playerEntity.setStatus(player.status);
    });

    if (playersMoved)
      this.trailsShape.graphics.clear()
        .beginBitmapFill(this.trailsCanvas, 'no-repeat')
        .drawRect(0, 0, Constants.BOARD_SIZE, Constants.BOARD_SIZE);

    if (noneAlive) {
      this.players.forEach((player: Player, id: string) => {
        player.tooltip.alpha = 1;
        player.tooltip.visible(true);
      });
    }
  }

  // Game actions
  startGame() {
	this.verifyOpen();
    this.gameService.send({ message: 'GAME_START' });
  }

  async requeue() {
    let partyId = sessionStorage.getItem('partyId');
    let isSpectator: boolean = sessionStorage.getItem('isSpectator') === 'true' ? true : false;
    if (isSpectator || partyId === null) {
      let roundId: string = sessionStorage.getItem('roundId');
      let nextRoundID: any = await this.http.get(`${environment.API_URL_GAME_ROUND}/${roundId}/requeue?isPlayer=${!isSpectator}`, { responseType: 'text' }).toPromise();
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
      let queueCallback = new EventSourcePolyfill(`${environment.API_URL_PARTY}/${partyId}/queue`, {});
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
    this.showLoader = true;
    setTimeout(() => {
      this.showLoader = false;
    }, (1000 * seconds));
  }

  verifyOpen() {
    if (!this.gameService.isOpen()) {
    	  console.log('GameService socket not open');
    	  this.ngZone.run(() => {
        this.router.navigate(['/login']);
      });
    }
  }

}
