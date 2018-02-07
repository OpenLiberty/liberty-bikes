import { Component, OnInit } from '@angular/core';
import { Whiteboard } from './whiteboard';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit {
  gameBoard: Whiteboard;
  constructor() { }

  ngOnInit() {
    this.gameBoard = new Whiteboard();
  }

  startGame() {
    this.gameBoard.startGame();
  }

  pauseGame() {
    this.gameBoard.pauseGame();
  }

  requeue() {
    this.gameBoard.requeue();
  }

}
