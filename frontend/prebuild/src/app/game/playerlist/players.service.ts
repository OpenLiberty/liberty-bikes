import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { SocketService } from '../../net/socket.service';


@Injectable()
export class PlayersService {
  public messages: Subject<Object>;

  constructor(socketService: SocketService) {

    this.messages = <Subject<Object>>socketService.socket
    .map((response: MessageEvent): any => {
      //console.log(`Players service handling message: ${response.data}`);
      return JSON.parse(response.data);
    });
  }

}
