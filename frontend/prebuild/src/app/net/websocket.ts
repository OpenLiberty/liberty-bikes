import * as $ from 'jquery';

export class GameWebsocket {
  websocket: WebSocket;
  public messageCallback: ((evt: MessageEvent) => any);
  public errorCallback: ((evt: MessageEvent) => any);
  public openCallback: ((evt: MessageEvent) => any);

  constructor(hostname: string, port: string, roundId: string) {

    let uri = `ws://${hostname}:${port}/round/ws/${roundId}`;
    this.websocket = new WebSocket(uri);

    this.websocket.onmessage = (evt: MessageEvent): any => {
      if (this.messageCallback != null) {
        this.messageCallback(evt);
      }
    };

    this.websocket.onerror = (evt: MessageEvent): any => {
      if (this.errorCallback != null) {
        this.errorCallback(evt);
      }
    };

    this.websocket.onopen = (evt: MessageEvent): any => {
      if (this.openCallback != null) {
        this.openCallback(evt);
      }
    };
  }

  sendJson(json: JSON) {
    this.sendText(JSON.stringify(json));
  }

  sendText(json: string) {
    console.log(`sending text: ${json}`);
    this.websocket.send(json);
  }
}
