import { Shape, Shadow } from 'createjs-module';

export class Obstacle {
  static readonly COLOR = '#AF0000';

  public shape: Shape = new Shape();
  obstaclePulseUp = true;

  constructor(public width: number, public height: number) {
  this.shape.shadow = new Shadow(Obstacle.COLOR, 0, 0, 20);
  this.shape.graphics.beginFill(Obstacle.COLOR).rect(0, 0, width, height);
  }

  public update(x: number, y: number) {
  this.shape.x = x;
  this.shape.y = y;
    let blur: number = this.shape.shadow.blur;
    blur += this.obstaclePulseUp ? 2 : -2;

    if (blur >= 50) {
      this.obstaclePulseUp = false;
    }

    if (blur <= 10) {
      this.obstaclePulseUp = true;
    }

    this.shape.shadow.blur = blur;
  }
}
