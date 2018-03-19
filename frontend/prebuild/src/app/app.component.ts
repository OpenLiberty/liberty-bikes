import { Component } from '@angular/core';
import { GameService } from './game/game.service';
import { SocketService } from './net/socket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  providers: [ SocketService, GameService ]
})
export class AppComponent {
  title = 'Liberty Bikes';
}
