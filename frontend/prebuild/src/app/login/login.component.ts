import { Component, OnInit, NgZone, HostBinding } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { trigger, animate, style, transition, group, query, stagger, state } from '@angular/animations';
import { environment } from './../../environments/environment';
import { PaneType } from '../slider/slider.component';
import { Player } from '../game/player/player';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  pane: PaneType = sessionStorage.getItem('username') === null ? 'left' : 'right';
  username: string;
  party: string;
  player = new Player('PLAYER NAME HERE', 'none', '#FFFFFF');

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
    
    this.route.params.subscribe( params => 
    {
      localStorage.setItem("jwt", params['jwt']);
    });
    if (localStorage.getItem('jwt') !== null && localStorage.getItem('jwt') !== 'undefined') {
      this.loginThroughGoogle();
    }
    
    if (sessionStorage.getItem('username') !== null) {
      this.username = sessionStorage.getItem('username');
      this.player.name = this.username;
    }
  }
  
  loginGoogle() {
       window.location.href = `http://${environment.API_URL_AUTH}/auth-service/GoogleAuth`;
  }

  async quickJoin() {
    // First get an unstarted round ID
    let roundID = await this.http.get(`${environment.API_URL_GAME_ROUND}/available`, { responseType: 'text' }).toPromise();
    // Then join the round
    this.joinRoundById(roundID);
  }

  async joinRound() {
    let roundID: any = await this.http.get(`${environment.API_URL_PARTY}/${this.party}/round`, { responseType: 'text' }).toPromise();
    console.log(`Got roundID=${roundID} for partyID=${this.party}`);
    this.joinRoundById(roundID);
  }

  async joinRoundById(roundID: string) {
    let ngZone = this.ngZone;
    let router = this.router;
    roundID = roundID.toUpperCase().replace(/[^A-Z]/g, '');
    let gameBoard = true;
    if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ) {
      // give a controller-only view on mobile devices
      gameBoard = false;
    }
    console.log(`Is this a mobile device? ${!gameBoard}`);

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
      if (data.gameState === 'FULL') {
        alert('Game round is Full!');
        return;
      }
      if (data.gameState === 'RUNNING') {
        alert('Game round has already started!');
        return;
      }
      if (data.gameState === 'FINISHED') {
        alert('Game round has already finished!');
        return;
      }

      let id = sessionStorage.getItem('userId');
      let response: any = await this.http.post(`${environment.API_URL_PLAYERS}/create?name=${this.username}&id=${id}`, '', {
        responseType: 'text'
      }).toPromise();      

      console.log(JSON.stringify(response));
      

      // TEMP: to prevent a race condition, putting this code inside of the player create callback to ensure that
      //       userId is set in the session storage before proceeding to the game board
      if (id === null) {
        sessionStorage.setItem('userId', response);
        sessionStorage.setItem('username', this.username);
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
  }

  loginAsGuest(username: string) {
    console.log(`Username input: "${username}"`);
    if (username.length < 1 || username.length > 20) {
      alert('Username must be between 1 and 20 chars');
      return;
    }
    this.player.name = username;
    this.username = username;
    sessionStorage.setItem('username', username);
    this.pane = 'right';
  }
  async loginThroughGoogle() {
    let jwt = localStorage.getItem("jwt");
    let user: any = await this.http.get(`${environment.API_URL_PLAYERS}/getJWTInfo`, { responseType: 'json', headers: new HttpHeaders({
        'Content-Type':  'application/json',
        'Authorization': 'Bearer ' + `${jwt}`
    }) }).toPromise(); 
    if (user.exists === 'true') {
      sessionStorage.setItem('username', user.username);
      sessionStorage.setItem('userId', user.id);
	  this.player.name = user.username;
    } else {
      var username = prompt("Choose a username:", "");
      this.player.name = username;
      sessionStorage.setItem('username', username);
      sessionStorage.setItem('userId', user.id);
      //register a new user
      let response: any = await this.http.post(`${environment.API_URL_PLAYERS}/create?name=${username}&id=${user.id}`, '', {
        responseType: 'text'
      }).toPromise();
    }    
    this.pane = 'right';
  
  }

  logout() {
    this.pane = 'left';
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('userId');
  }

  cancelLogin() {
    this.pane = 'left';
  }
}
