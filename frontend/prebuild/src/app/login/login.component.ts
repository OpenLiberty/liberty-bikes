import { Component, OnInit, NgZone } from '@angular/core';
import { Router } from '@angular/router';

import * as $ from 'jquery';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  constructor(private router: Router, private ngZone: NgZone) {

  }

  ngOnInit() {}

  createRound() {
    $.post(`http://${document.location.hostname}:8080/round/create`, function(data) {
      alert(`Created round: ${data}`);
    });
  }

  joinRound(gameBoard: boolean) {
    let ngZone = this.ngZone;
    let router = this.router;
    let roundID: string = $('#roundid').val();
    roundID = roundID.toUpperCase().replace(/[^A-Z]/g, '');
    if (roundID.length !== 6) {
      alert(roundID + ' is not a valid round ID, because it must be 6 letters long');
      return;
    }

    $.get(`http://${document.location.hostname}:8080/round/${roundID}`, function(data) {
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

      sessionStorage.setItem('username', $('#username').val());
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
  });
  }

  hostRound() {
    // TODO: update the button on click to indicate a waiting state in case
    // this post request takes a noticeable amount of time
    let ngZone = this.ngZone;
    let router = this.router;
    $.post(`http://${document.location.hostname}:8080/round/create`, function(data) {
      console.log(`Created round with id=${data}`);
      sessionStorage.setItem('isSpectator', 'true');
      sessionStorage.setItem('roundId', data);
      ngZone.run(() => {
        router.navigate(['/game']);
      });
    });
  }
}
