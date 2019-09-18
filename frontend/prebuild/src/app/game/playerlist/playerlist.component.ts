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
        // console.log(`Got playerlist ${JSON.stringify(json.playerlist)}`);
        json.playerlist.forEach((player, i) => {
          if (this.players.length > i) {
            // Player already exists, compare
            if (this.players[i].name !== player.name) {
              this.players[i].name = player.name;
            }

            if (this.players[i].status !== player.status) {
              this.players[i].status = player.status;
            }

            if (this.players[i].color !== player.color) {
              this.players[i].color = player.color;
            }
          } else {
            const newPlayer = new Player();
            newPlayer.name = player.name;
            newPlayer.status = player.status;
            newPlayer.color = player.color;
            this.players.push(newPlayer);
          }
        });
      }
    }, (err) => {
      console.log(`Error occurred: ${err}`);
    });
  }

  ngOnInit() {

  }
}
