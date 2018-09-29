import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { SocketService } from '../../net/socket.service';
import { map } from 'rxjs/operators';


@Injectable()
export class PlayersService {
  public messages: Subject<Object>;

  constructor(socketService: SocketService) {

    this.messages = <Subject<Object>>socketService.socket
    .pipe(map((response: MessageEvent): any => {
      return JSON.parse(response.data);
    }));
  }

}
