import { Shape } from 'createjs-module';

export class Obstacle {
  static readonly COLOR = '#808080';
  
  public shape: Shape = new Shape();
  
  constructor(public width: number, public height: number) {
	this.shape.graphics.beginFill(Obstacle.COLOR).rect(0, 0, width, height);
  }
  
  public update(x: number, y: number) {
	this.shape.x = x;
	this.shape.y = y;
  }
}
