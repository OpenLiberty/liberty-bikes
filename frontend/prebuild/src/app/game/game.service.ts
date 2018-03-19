import { Injectable } from '@angular/core';
import { SocketService } from '../net/socket.service';
import { Subject } from 'rxjs/Subject';
import 'rxjs/add/operator/map';



@Injectable()
export class GameService {
  public messages: Subject<Object>;

  roundId: string;
  serverHost: string;
  serverPort: string;

  constructor(socketService: SocketService) {
    this.roundId = sessionStorage.getItem('roundId');
    console.log(`Round ID: ${this.roundId}`);
    this.serverHost = document.location.hostname;
    this.serverPort = '8080';

    socketService.url = `ws://${this.serverHost}:${this.serverPort}/round/ws/${this.roundId}`;
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
