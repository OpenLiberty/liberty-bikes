import { Component, OnInit, OnDestroy, NgZone, HostBinding, Injectable } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EventSourcePolyfill } from 'ng-event-source';
import { trigger, animate, style, transition, group, query, stagger, state } from '@angular/animations';
import { environment } from './../../environments/environment';
import { PaneType } from '../slider/slider.component';
import { Player } from '../entity/player';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {
  static queueCallback: EventSourcePolyfill;

  pane: PaneType = sessionStorage.getItem('username') === null ? 'left' : 'right';
  username: string;
  party: string;
  queuePosition: number;
  player = new Player();
  isFullDevice: boolean = !/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

  constructor(
    private router: Router,
    private ngZone: NgZone,
    private meta: Meta,
    private http: HttpClient,
    private route: ActivatedRoute
  ) {}


  ngOnInit() {
    this.meta.removeTag('viewport');
    let viewWidth = window.innerWidth;
    let viewHeight = window.innerHeight;

    this.meta.addTag({name: 'viewport', content: `width=${viewWidth}, height=${viewHeight}, initial-scale=1.0`}, true);

    this.route.params.subscribe( params =>  {
      if (params['jwt']) {
        sessionStorage.setItem('jwt', params['jwt']);
      }
    });
    let jwt = sessionStorage.getItem('jwt');
    if (jwt) {
      this.getPlayerId(jwt);
    }

    this.player.status = 'none';

    if (sessionStorage.getItem('username') !== null) {
      this.username = sessionStorage.getItem('username');
      this.player.name = this.username;
    }

    // If a player has participated in a game and requeued and the next round is full, they will be redirected back to the login page.
    // Re-use the EventSource initiated on the game page, but change the onMessage() function to match the context of this page
    let queuePosition = sessionStorage.getItem('queuePosition');
    if (queuePosition) {
      sessionStorage.removeItem('queuePosition');
      console.log(`User already associated with party, entering queue`);
      this.setQueueOnMessage();
      this.showQueue(queuePosition);
    }
  }

  ngOnDestroy() {
    this.cancelQueue();
  }

  loginGoogle() {
       window.location.href = `${environment.API_URL_AUTH}/auth-service/GoogleAuth`;
  }

  loginGithub() {
      window.location.href = `${environment.API_URL_AUTH}/auth-service/GitHubAuth`;
  }

  loginTwitter() {
      window.location.href = `${environment.API_URL_AUTH}/auth-service/TwitterAuth`;
  }

  async quickJoin() {
    // First get an unstarted round ID
    let roundID = await this.http.get(`${environment.API_URL_GAME_ROUND}/available`, { responseType: 'text' }).toPromise();
    // Then join the round
    this.joinRoundById(roundID);
  }

  async joinParty() {
    let roundID: any = await this.http.get(`${environment.API_URL_PARTY}/${this.party}/round`, { responseType: 'text' }).toPromise();
    console.log(`Got roundID=${roundID} for partyID=${this.party}`);
    if (!roundID) {
      alert(`Party ${this.party} does not exist.`);
      return;
    }
    sessionStorage.setItem('partyId', this.party);
    this.joinRoundById(roundID);
  }

  async joinRoundById(roundID: string) {
    let ngZone = this.ngZone;
    let router = this.router;
    roundID = roundID.toUpperCase().replace(/[^A-Z]/g, '');
    // give a controller-only view on mobile devices
    let gameBoard = this.isFullDevice;
    console.log(`Is this a mobile device? ${!this.isFullDevice}`);

    // TODO: Validate form input in a more elegant way than alert()
    if (roundID.length !== 4) {
      alert(roundID + ' is not a valid round ID, because it must be 4 letters long');
      return;
    }

    try {
      let data: any = await this.http.get(`${environment.API_URL_GAME_ROUND}/${roundID}`).toPromise();
      console.log(JSON.stringify(data));
      if (data === null) {
        alert('Game round does not exist!');
        return;
      }

      if (data.gameState !== 'OPEN') {
        if (this.party === null) {
          if (data.gameState === 'FULL') {
            alert('All games are full!  Try again in a few seconds.');
          } else {
            alert('Game has already begun!  Try again later.');
          }
        } else {
          this.enterQueue();
        }
        return;
      }
      sessionStorage.setItem('isSpectator', 'false');
      sessionStorage.setItem('roundId', roundID);
      if (gameBoard === true) {
        ngZone.run(() => {
          router.navigate(['/game']);
        });
      } else {
        ngZone.run(() => {
          router.navigate(['/play']);
        });
      }

    } catch (error) {
      console.log(error);
    }
  }

  async hostRound() {
    // TODO: update the button on click to indicate a waiting state in case
    // this post request takes a noticeable amount of time
    let ngZone = this.ngZone;
    let router = this.router;
    try {
      let party: any = await this.http.post(`${environment.API_URL_PARTY}/create`, '', { responseType: 'json' }).toPromise();

      console.log(`Created round with id=${party}`);
      sessionStorage.setItem('isSpectator', 'true');
      sessionStorage.setItem('partyId', party.id);
      sessionStorage.setItem('roundId', party.currentRound.id);
      ngZone.run(() => {
        router.navigate(['/game']);
      });
    } catch (error) {
      console.log(error);
    }
  }

  showGuestLogin() {
    this.pane = 'center';
    document.getElementById('signin').innerHTML = 'Sign In As Guest';
  }

  setQueueOnMessage() {
    LoginComponent.queueCallback.onmessage = msg => {
      let queueMsg = JSON.parse(msg.data);
      if (queueMsg.queuePosition) {
        this.showQueue(queueMsg.queuePosition);
      } else if (queueMsg.requeue) {
        console.log(`ready to join game! Joining round ${queueMsg.requeue}`);
        LoginComponent.queueCallback.close();
        this.joinRoundById(queueMsg.requeue);
      } else {
        console.log('Error: unrecognized message  ' + msg.data);
      }
    };
  }

  enterQueue() {
    console.log(`enering queue for party ${this.party}`);
    if (LoginComponent.queueCallback) {
      LoginComponent.queueCallback.close();
    }
    LoginComponent.queueCallback = new EventSourcePolyfill(`${environment.API_URL_PARTY}/${this.party}/queue`, {});
    this.setQueueOnMessage();
    LoginComponent.queueCallback.onerror = msg => {
      console.log('Error showing queue position: ' + JSON.stringify(msg.data));
    };
  }

  showQueue(queuePosition) {
    this.ngZone.run(() => {
      this.queuePosition = queuePosition;
      console.log(`Still waiting in queue at position ${this.queuePosition}`);
      this.pane = 'queue';
    });
  }

  cancelQueue() {
    if (LoginComponent.queueCallback) {
      try {
        LoginComponent.queueCallback.close();
        this.pane = 'right';
      } catch (ignore) { }
    }
  }

  async loginAsGuest(username: string) {
    if (await this.createUser(username, sessionStorage.getItem('userId'))) {
      this.pane = 'right';
    }
  }

  async getPlayerId(jwt: string) {
    let user: any = await this.http.get(`${environment.API_URL_PLAYERS}/getJWTInfo`, { responseType: 'json', headers: new HttpHeaders({
        'Content-Type':  'application/json',
        'Authorization': 'Bearer ' + `${jwt}`
    }) }).toPromise();
    sessionStorage.setItem('userId', user.id);
    if (user.exists === 'true') {
      sessionStorage.setItem('username', user.username);
      this.player.name = user.username;
      this.pane = 'right';
    } else {
      this.pane = 'center';
      document.getElementById('signin').innerHTML = 'register username';
    }
  }

  async createUser(username: string, userid: string) {
    console.log(`Username input: "${username}"`);

    // ensure username is valid before creating
    let usernameError = this.validateUsername(username);
    if (usernameError !== null) {
      alert(usernameError);
      return false;
    }
    username = username.trim();

    // register a new user
    let createdUserId: any = await this.http.post(`${environment.API_URL_PLAYERS}/create?name=${username}&id=${userid}`, '', {
      responseType: 'text'
    }).toPromise();
    console.log('Created player: ' + JSON.stringify(createdUserId));
    this.player.name = username;
    sessionStorage.setItem('username', username);
    sessionStorage.setItem('userId', createdUserId);
    return true;
  }

  validateUsername(username: string) {
    if (username === undefined || username.trim().length < 1 || username.trim().length > 20) {
      return 'Username must be between 1 and 20 chars';
    }
    let usernameRegex: RegExp = /^[a-zA-Z0-9 -]{1,20}$/;
    if (null == username.match(usernameRegex)) {
      return 'Username must consist of characters A-Z, a-z, 0-9, \' \', and \'-\'';
    }
    // Username is valid
    return null;
  }

  logout() {
    this.pane = 'left';
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('userId');
  }

  cancelLogin() {
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('userId');
    this.pane = 'left';
  }
}
