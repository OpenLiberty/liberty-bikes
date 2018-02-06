import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import * as $ from 'jquery';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  constructor(private router: Router) {

  }

  ngOnInit() {}

  createRound() {
    $.post(`http://${document.location.hostname}:8080/round/create`, function(data) {
      alert('Response is: ' + data);
    });
  }

  joinRound() {
    localStorage.setItem('username', $('#username').val());
    localStorage.setItem('roundId', $('#roundid').val());
    this.router.navigate(['/game']);
  }
}
