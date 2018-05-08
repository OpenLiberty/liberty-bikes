import { Shape } from 'createjs-module';
import { PlayerTooltip } from './player.tooltip';

export class Player {
  static BOX_SIZE: number = 5;
	
  public name: string;
  public status: string;
  public color: string;
  public shape: Shape = new Shape();
  public tooltip: PlayerTooltip;
  
  public draw(width: number, height: number) {
	  this.tooltip = new PlayerTooltip(this);
      this.shape.graphics.beginFill(this.color)
                              .rect(0, 0, width * Player.BOX_SIZE, height * Player.BOX_SIZE);
      this.shape.graphics.beginFill('#e8e5e5')
                              .rect(width / 4 * Player.BOX_SIZE, height / 4 * Player.BOX_SIZE,
                                    Player.BOX_SIZE * (width / 2), Player.BOX_SIZE * (height / 2));
  }
  
  public update(x, y) {
	this.shape.x = x;
	this.shape.y = y;
	this.tooltip.update();
  }
  
  public visible(isVisible: boolean) {
	this.shape.visible = isVisible;
	this.tooltip.visible(isVisible);
  }
}
