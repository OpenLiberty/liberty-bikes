import { Shape, Text } from 'createjs-module';
import { Player } from './player';

export class PlayerTooltip {
  public shape: Shape = new Shape();
  public nameText: Text;
  public lifetime: number = 30;

  showAbove: boolean;
  lastY: number = 0; // track if player is moving up or down

  constructor(public player: Player) {
	if (!this.player.color)
      console.log('WARNING: Creating a tooltip with a player that is not properly initialized');
    this.nameText = new Text(this.player.name, "14px Arial", "#fff");
    this.nameText.textAlign = 'center';
    this.nameText.maxWidth = 100;
	this.shape.graphics.beginFill(this.player.color)
	                   .drawRoundRect(0,0, 115, 25, 10);
  }
  
  public update() {
	// positioning
	if (this.lastY < this.player.y)
	  this.showAbove = true;
	else if (this.lastY > this.player.y)
	  this.showAbove = false;
	
	if (this.player.y < 50)
      this.showAbove = false;
    else if (this.player.y > (600 - 50))
    	  this.showAbove = true;
	
	this.lastY = this.player.y;
	this.shape.x = this.player.x - 50;
	this.shape.y = this.player.y + (this.showAbove ? -48 : 36);
	this.nameText.x = this.player.x + 8;
	this.nameText.y = this.player.y + (this.showAbove ? -45 : 39);
	
	// prevent tooltip from going off screen
	if (this.shape.x < 0)
	  this.shape.x = 0;
	if ((this.shape.x + 115) > 600)
	  this.shape.x = (600 - 115);
	if (this.nameText.x - 50 < 0)
	  this.nameText.x = 50;
	if (this.nameText.x + 50 > 600)
	  this.nameText.x = (600 - 50);
	
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
