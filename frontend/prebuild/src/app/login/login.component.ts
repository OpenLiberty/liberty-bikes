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
  styleUrls: ['./login.component.scss'],
  animations: [
    trigger('sso', [
      transition(':enter', [
        style({height: '0', opacity: 0, 'padding-bottom': 0}),
        animate('300ms', style({height: '*', opacity: 1, 'padding-bottom': '*'}))
      ]),
      transition(':leave', [
        style({height: '*', opacity: 1, 'padding-bottom' : '*'}),
        animate('300ms', style({height: '0', opacity: 0, 'padding-bottom': 0}))
      ])
    ])
  ]
})
export class LoginComponent implements OnInit, OnDestroy {
  static queueCallback: EventSourcePolyfill;

  pane: PaneType = sessionStorage.getItem('username') === null ? 'left' : 'right';
  username: string;
  queuePosition: number;
  player = new Player();
  nextAiName: string;
  aiName: string;
  hasBotKey = false;
  botKey: string;

  private partyCode = '';
  get party(): string {
    return this.partyCode.toLocaleUpperCase().trim();
  }
  set party(newParty: string) {
    this.partyCode = newParty;
  }

  isFullDevice: boolean = !/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
  isQuickPlayAllowed: boolean = this.isFullDevice;
  isSingleParty = false;
  isSsoCheckComplete = false;
  isGoogleConfigured = false;
  isGithubConfigured = false;
  isTwitterConfigured = false;
  isPerformingSsoRedirect = false;

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

    this.checkForSingleParty();
    this.checkSsoOptions();
  }

  ngOnDestroy() {
    this.cancelQueue();
  }

  loginGoogle() {
    this.doSsoLogin(`${environment.API_URL_AUTH}/auth-service/GoogleAuth`);
  }

  loginGithub() {
    this.doSsoLogin(`${environment.API_URL_AUTH}/auth-service/GitHubAuth`);
  }

  loginTwitter() {
    this.doSsoLogin(`${environment.API_URL_AUTH}/auth-service/TwitterAuth`);
  }

  async doSsoLogin(href) {
    this.isPerformingSsoRedirect = true;

    // Delay to let the animation complete
    await new Promise((resolve) => setTimeout(resolve, 500));

    window.location.href = href;
  }

  async checkSsoOptions() {
    // Delay to give users enough time to read the check text
    await new Promise((resolve) => setTimeout(resolve, 1000));

    let data: any = await this.http.get(`${environment.API_URL_AUTH}/auth-service`).toPromise();
    if (data == null) {
      console.log('WARNING: Unable to contact auth service to determine SSO options');
      this.ngZone.run(() => {
        this.isSsoCheckComplete = true;
      });
      return;
    }

    console.log('Configured auth schemes: ' + JSON.stringify(data));
    this.ngZone.run(() => {
      if (data.indexOf('TwitterAuth') > -1) {
        this.isTwitterConfigured = true;
      }
      if (data.indexOf('GoogleAuth') > -1) {
        this.isGoogleConfigured = true;
      }
      if (data.indexOf('GitHubAuth') > -1) {
        this.isGithubConfigured = true;
      }

      this.isSsoCheckComplete = true;
    });
  }

  async checkForSingleParty() {
    if (this.isSingleParty) {
      sessionStorage.setItem('isSingleParty', String(this.isSingleParty));
      return;
    }

    let data: any = await this.http.get(`${environment.API_URL_PARTY}/describe`).toPromise();
    if (data == null) {
      console.log('WARNING: Unable to contact party service to determine if single party mode is enabled');
      return;
    }

    if (data.isSingleParty === true) {
      console.log('Single party mode enabled');
      this.party = data.partyId;
      this.ngZone.run(() => {
        this.isSingleParty = true;
        this.isQuickPlayAllowed = true;
      });
    } else {
      console.log('Single party mode disabled');
    }

    sessionStorage.setItem('isSingleParty', String(this.isSingleParty));
  }

  async quickJoin() {
    if (this.isSingleParty) {
      this.joinParty();
      return;
    }
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
      let party: any = await this.http.post(`${environment.API_URL_PARTY}`, '', { responseType: 'json' }).toPromise();

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
    let playerId = sessionStorage.getItem('userId');
    LoginComponent.queueCallback = new EventSourcePolyfill(`${environment.API_URL_PARTY}/${this.party}/queue?playerId=${playerId}`, {});
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
    let createdUserId: any = await this.http.post(`${environment.API_URL_PLAYERS}?name=${username}&id=${userid}`, '', {
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
    let usernameRegex: RegExp = /^[\x61-\x7A\x41-\x5A\x30-\x39\xC0-\xFF -]{1,20}$/;
    if (null == username.match(usernameRegex)) {
      return 'Username must consist of characters A-Z, a-z, 0-9, \' \', and \'-\'';
    }
    // Username is valid
    return null;
  }

  logout() {
    this.pane = 'left';
    this.username = null;
    this.player = new Player();
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('userId');
  }

  cancelLogin() {
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('userId');
    this.pane = 'left';
  }

  makeId() {
   let result           = '';
   let characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
   let charactersLength = characters.length;
   for (let i = 1; i <= 16; i++) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
      if (i % 4 === 0 && i !== 16) {
          result += '-';
      }
   }
   return result;
  }

  showBotLogin() {
     this.pane = 'ai';
  }

  async doneRegisteringBots() {
    this.pane = 'left';
    await new Promise((resolve) => setTimeout(resolve, 500));
    this.hasBotKey = false;
    this.botKey = null;
    this.aiName = null;
    this.nextAiName = null;
  }

  async registerBot(name: string) {
    console.log(`Bot name input: "${name}"`);

    // ensure username is valid before creating
    let usernameError = this.validateUsername(name);
    if (usernameError !== null) {
      alert(usernameError);
      return false;
    }
    name = name.trim();

    let userId = this.makeId();
    console.log(`id: ${userId}`);
    // register a new user
    let key: any = await this.http.post(`${environment.API_URL_PLAYERS}?name=${name}&id=${userId}`, '', {
      responseType: 'text'
    }).toPromise();

    this.hasBotKey = true;
    this.botKey = userId;
    this.aiName = name;
  }
}
