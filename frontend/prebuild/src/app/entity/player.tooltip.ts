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
	if (this.lastY > this.player.shape.y)
		this.showAbove = true;
	else if (this.lastY < this.player.shape.y)
		this.showAbove = false;
	this.lastY = this.player.shape.y;
	this.shape.x = this.player.shape.x - 50;
	this.shape.y = this.player.shape.y + (this.showAbove ? 20 : -35);
	this.nameText.x = this.player.shape.x + 8;
	this.nameText.y = this.player.shape.y + (this.showAbove ? 23 : -32);
	
	// start fading out after 15 ticks
	if(this.lifetime > 0)
	  this.lifetime--;
	if (this.lifetime < 15) {
	  this.shape.alpha = (this.lifetime / 10);
	  this.nameText.alpha = (this.lifetime / 10);
	}
  }
  
  public visible(isVisible: boolean) {
	this.shape.visible = isVisible;
	this.nameText.visible = isVisible;
  }
}
