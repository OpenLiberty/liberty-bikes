import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';
import 'rxjs/add/operator/share';

@Injectable()
export class SocketService {

  private socketUrl: string;
  private subject: Subject<MessageEvent>;
  private hasUrl = false;
  private open = false;
  private messageBuffer: string[];

  get url(): string {
    return this.socketUrl;
  }

  set url(newUrl: string) {
    console.log(`Setting url to ${newUrl}`);
    if (this.socketUrl !== newUrl) {
      console.log(`Updating socket with new URL`);
      this.open = false;

      this.hasUrl = false;
      this.subject = this.create(newUrl);
      this.socketUrl = newUrl;
      this.hasUrl = true;
    }
  }

  constructor() {
    this.messageBuffer = [];
   }

  get socketOpen(): boolean {
    return this.open;
  }

  get initialized(): boolean {
    return this.hasUrl;
  }

  get socket(): Subject<MessageEvent> {
    console.log(`retrieving socket: ${this.subject}`);
    if (!this.initialized) {
      console.warn("Socket not initialized yet");
    }
    return this.subject;
  }

  private create(url): Subject<MessageEvent> {
    console.log(`Creating new socket for ${url}`);
    const ws = new WebSocket(url);

    ws.onopen = () => {
      console.log('Socket open, sending buffered messages')
      this.open = true;
      while (this.messageBuffer.length > 0) {
        this.subject.next(this.messageBuffer.shift() as any);
      }
    };

    const observable = Observable.create(
      (obs: Observer<MessageEvent>) => {
        ws.onmessage = obs.next.bind(obs);
        ws.onerror = obs.error.bind(obs);
        ws.onclose = obs.complete.bind(obs);

        return ws.close.bind(ws);
      }
    );

    const observer = {
      next: (data: string) => {
        if (ws.readyState === ws.OPEN) {
          console.log(`sending text: ${data}`);
          ws.send(data);
        } else {
          console.log('socket not open, buffering message');
          this.open = false;
          this.messageBuffer.push(data);
        }
      }
    };

    return Subject.create(observer, observable.share());
  }

}
