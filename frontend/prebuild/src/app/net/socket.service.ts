import { Injectable } from '@angular/core';
import { Subject, Observable, Observer } from 'rxjs';
import 'rxjs/add/operator/share';


@Injectable()
export class SocketService {

  private ws;
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
    if (this.socketOpen) {
      this.close();
    }

    if (this.socketUrl !== newUrl) {
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
      console.warn('Socket not initialized yet');
    }
    return this.subject;
  }

  public close() {
    this.open = false;
    this.ws.close();
  }

  private create(url): Subject<MessageEvent> {
    console.log(`Creating new socket for ${url}`);
    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      console.log('Socket open, sending buffered messages');
      this.open = true;
      while (this.messageBuffer.length > 0) {
        this.subject.next(this.messageBuffer.shift() as any);
      }
    };

    this.ws.onclose = () => {
      console.log('Socket closed');
      this.open = false;
    };

    const observable = Observable.create(
      (obs: Observer<MessageEvent>) => {
        this.ws.onmessage = obs.next.bind(obs);
        this.ws.onerror = obs.error.bind(obs);
        this.ws.onclose = obs.complete.bind(obs);

        return this.ws.close.bind(this.ws);
      }
    );

    const observer = {
      next: (data: string) => {
        if (this.ws.readyState === this.ws.OPEN) {
          console.log(`sending text: ${data}`);
          this.ws.send(data);
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
