import { Component, OnInit, NgZone } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import * as $ from 'jquery';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  constructor(
    private router: Router,
    private ngZone: NgZone,
    private meta: Meta,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.meta.removeTag('viewport');
    let viewWidth = $(window).width();
    let viewHeight = $(window).height();

    this.meta.addTag({name: 'viewport', content: `width=${viewWidth}px, height=${viewHeight}px, initial-scale=1.0`}, true);
  }

  async createRound() {
    try {
      let data = await this.http.post(`http://${document.location.hostname}:8080/round/create`, "", { responseType: 'text'}).toPromise();
      alert(`Created round: ${data}`);
    } catch (error) {
      console.log(error);
    }

  }

async joinRound(gameBoard: boolean) {
  let ngZone = this.ngZone;
  let router = this.router;
  let roundID: string = $('#roundid').val();
  let username: string = $('#username').val();
  roundID = roundID.toUpperCase().replace(/[^A-Z]/g, '');
  // TODO: Validate form input in a more elegant way than alert()
  if (roundID.length !== 4) {
    alert(
      roundID + ' is not a valid round ID, because it must be 4 letters long'
    );
    return;
  }
  if (username.length < 1) {
    alert('Username must not be empty');
    return;
  }

  try {
    let data: any = await this.http.get(`http://${document.location.hostname}:8080/round/${roundID}`).toPromise();
    console.log(JSON.stringify(data));
    if (data === undefined) {
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

    let response: any = await this.http.post(`http://${document.location.hostname}:8081/player/create?name=${username}`, "", {
    responseType: 'text'
    }).toPromise();
    console.log(JSON.stringify(response));
    console.log(`Created a new player with ID=${response}`);
    sessionStorage.setItem('userId', response);

    // TEMP: to prevent a race condition, putting this code inside of the player create callback to ensure that
    //       userId is set in the session storage before proceeding to the game board
    sessionStorage.setItem('username', username);
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
      let data = await this.http.post(`http://${document.location.hostname}:8080/round/create`, "", { responseType: 'text' }).toPromise();
      console.log(`Created round with id=${data}`);
      sessionStorage.setItem('isSpectator', 'true');
      sessionStorage.setItem('roundId', data);
      ngZone.run(() => {
        router.navigate(['/game']);
      });
    } catch (error) {
      console.log(error);
    }
  }
}
