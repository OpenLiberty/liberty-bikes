import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';
import { GameComponent } from './game/game.component';
import { ControlsComponent } from './controls/controls.component';

import { environment } from './../environments/environment';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'login/:jwt', component: LoginComponent },
  { path: 'game', component: GameComponent },
  { path: 'play', component: ControlsComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full'}
];

@NgModule({
  imports: [ RouterModule.forRoot(routes, {}) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {}
