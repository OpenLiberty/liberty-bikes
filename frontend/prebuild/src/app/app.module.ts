import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';

import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { GameComponent } from './game/game.component';
import { ControlsComponent } from './controls/controls.component';
import { PlayerListComponent } from './game/playerlist/playerlist.component';
import { PlayerComponent } from './game/player/player.component';
import { LeaderboardComponent } from './game/leaderboard/leaderboard.component';

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    NgbModule.forRoot(),
    HttpClientModule
  ],
  declarations: [
    AppComponent,
    LoginComponent,
    GameComponent,
    ControlsComponent,
    PlayerListComponent,
    PlayerComponent,
    LeaderboardComponent,
  ],
  providers: [ ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
