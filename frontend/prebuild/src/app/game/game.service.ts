import { Injectable } from '@angular/core';
import { SocketService } from '../net/socket.service';
import { Subject } from 'rxjs/Subject';
import { environment } from './../../environments/environment';
import 'rxjs/add/operator/map';



@Injectable()
export class GameService {
  public messages: Subject<Object>;

  roundId: string;

  constructor(socketService: SocketService) {
    this.roundId = sessionStorage.getItem('roundId');
    console.log(`Round ID: ${this.roundId}`);

    socketService.url = `${environment.API_URL_GAME_WS}/${this.roundId}`;
    this.messages = <Subject<Object>>socketService.socket
    .map((response: MessageEvent): any => {
      console.log(`Game service handling message: ${response.data}`);
      return JSON.parse(response.data);
    });
  }

  public send(message: any) {
    this.messages.next(JSON.stringify(message));
  }
}
