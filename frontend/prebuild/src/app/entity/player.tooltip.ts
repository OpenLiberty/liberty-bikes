import { Shape, Text } from 'createjs-module';
import { Player } from './player';
import { Constants } from '../game/constants';

export class PlayerTooltip {
  public shape: Shape = new Shape();
  public nameText: Text;
  public lifetime: number = 30;

  showAbove: boolean;
  lastY: number = 0; // track if player is moving up or down

  constructor(public player: Player) {
	if (!this.player.color)
      console.log('WARNING: Creating a tooltip with a player that is not properly initialized');
    this.nameText = new Text(this.player.name, "28px Arial", "#000");
    this.nameText.textAlign = 'center';
    this.nameText.maxWidth = 200;
	this.shape.graphics.beginFill(this.player.color)
	                   .drawRoundRect(0,0, 230, 50, 10);
  }

  public update() {
	// positioning
	if (this.lastY < this.player.y)
	  this.showAbove = true;
	else if (this.lastY > this.player.y)
	  this.showAbove = false;

	if (this.player.y < 100)
      this.showAbove = false;
    else if (this.player.y > (Constants.BOARD_SIZE - 100))
    	  this.showAbove = true;

	this.lastY = this.player.y;
	this.shape.x = this.player.x - 100;
	this.shape.y = this.player.y + (this.showAbove ? -96 : 72);
	this.nameText.x = this.player.x + 16;
	this.nameText.y = this.player.y + (this.showAbove ? -90 : 78);

	// prevent tooltip from going off screen
	if (this.shape.x < 0)
	  this.shape.x = 0;
	if ((this.shape.x + 230) > Constants.BOARD_SIZE)
	  this.shape.x = (Constants.BOARD_SIZE - 230);
	if (this.nameText.x - 100 < 0)
	  this.nameText.x = 100;
	if (this.nameText.x + 100 > Constants.BOARD_SIZE)
	  this.nameText.x = (Constants.BOARD_SIZE - 100);

	// start fading out after 15 ticks
	if (this.player.status === 'Alive') {
	  if(this.lifetime > 0)
	    this.lifetime--;
	  if (this.lifetime < 15) {
	    this.alpha(this.lifetime / 15);
	  }
	} else {
	  this.alpha(0.9);
	}
  }

  public visible(isVisible: boolean) {
	this.shape.visible = isVisible;
	this.nameText.visible = isVisible;
  }

  public alpha(newAlpha: number) {
	  if (newAlpha > 0.9)
		newAlpha = 0.9;
	  this.shape.alpha = newAlpha;
	  this.nameText.alpha = newAlpha;
  }
}
