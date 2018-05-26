import { Component, OnInit, NgZone } from '@angular/core';
import { Player } from '../../entity/player';
import { PlayersService } from './players.service';

@Component({
  selector: 'app-player-list',
  templateUrl: './playerlist.component.html',
  styleUrls: ['./playerlist.component.scss'],
  providers: [ PlayersService ],
})
export class PlayerListComponent implements OnInit {
  players: Player[] = new Array();

  constructor(private playersService: PlayersService, private ngZone: NgZone) {
    playersService.messages.subscribe((msg) => {
      const json = msg as any;
      if (json.playerlist) {
        const newPlayers = new Array();
        // console.log(`Got playerlist ${JSON.stringify(json.playerlist)}`);
        for (let player of json.playerlist) {
          const newPlayer = new Player();
          newPlayer.name = player.name;
          newPlayer.status = player.status;
          newPlayer.color = player.color;
          newPlayers.push(newPlayer);
        }
        this.ngZone.run(() => {
          this.players = newPlayers;
        });
      }
    }, (err) => {
      console.log(`Error occurred: ${err}`);
    });
  }

  ngOnInit() {

  }
}
