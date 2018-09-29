import { Injectable } from '@angular/core';
import { SocketService } from '../net/socket.service';
import { Subject } from 'rxjs';
import { environment } from './../../environments/environment';
import { map } from 'rxjs/operators';



@Injectable()
export class GameService {
  public messages: Subject<Object>;

  roundId: string;

  constructor(private socketService: SocketService) {
    this.roundId = sessionStorage.getItem('roundId');
    console.log(`Round ID: ${this.roundId}`);

    socketService.url = `${environment.API_URL_GAME_WS}/${this.roundId}`;
    this.messages = <Subject<Object>>socketService.socket
    .pipe(map((response: MessageEvent): any => {
      return JSON.parse(response.data);
    }));
  }

  public send(message: any) {
    this.messages.next(JSON.stringify(message));
  }

  public isOpen() {
    return this.socketService.socketOpen;
  }

  public close() {
    this.socketService.close();
  }
}
