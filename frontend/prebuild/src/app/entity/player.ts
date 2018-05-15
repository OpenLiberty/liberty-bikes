import { Shape, Bitmap, Stage } from 'createjs-module';
import { PlayerTooltip } from './player.tooltip';

export class Player {
  // According to EaselJS, "When a string path or image tag that is not yet loaded is used, the stage may need to be redrawn before the Bitmap will be displayed."
  // For this reason, we need to pre-load an instance of the image (this copy never gets used)
  static readonly PLAYER_BITMAP = new Bitmap('../../assets/images/bike_wide.png');
  static readonly PLAYER_DEAD_BITMAP = new Bitmap('../../assets/images/status_dead.png');
  static readonly BAM = new Audio('../../assets/sound/bam.wav');
  static audioLoaded: boolean = false;
  
  public name: string;
  public status: string;
  public color: string;
  public x: number;
  public y: number;
  public image: Bitmap;
  public tooltip: PlayerTooltip;

  public update(x: number, y: number, direction: string) {
	//console.log(`[Player-${this.name}]  x=${x}  y=${y}  direction=${direction}`);
    this.x = x;
    this.y = y;
	if (!this.tooltip)
	  this.tooltip = new PlayerTooltip(this);
	if (!this.image) {
    this.image = new Bitmap('../../assets/images/bike_wide.png');
    this.image.scaleX = 2.0;
    this.image.scaleY = 2.0;
	}

	if (direction === 'UP') {
      this.image.rotation = 0;
  	  this.image.x = x - 10;
  	  this.image.y = y - 10;
	} else if (direction === 'RIGHT') {
      this.image.rotation = 90;
  	  this.image.x = x + 40;
  	  this.image.y = y - 10;
	} else if (direction === 'DOWN') {
      this.image.rotation = 180;
  	  this.image.x = x + 40;
  	  this.image.y = y + 40;
	} else {
      this.image.rotation = 270;
  	  this.image.x = x - 10;
  	  this.image.y = y + 40;
	}
	this.tooltip.update();
  }

  public visible(isVisible: boolean) {
    this.image.visible = isVisible;
    this.tooltip.visible(isVisible);
  }

  public setStatus(status: string) {
    if (status === 'Dead' && this.status !== 'Dead') {
    	  this.image = new Bitmap('../../assets/images/status_dead.png');
    	  this.image.scaleX = 1.2;
    	  this.image.scaleY = 1.2;
    	  this.image.x = this.x - 20;
    	  this.image.y = this.y - 20;
      if (!Player.audioLoaded) {
        Player.audioLoaded = true;
        Player.BAM.load();
      }
    	  Player.BAM.play();
    }
    this.status = status;
  }

  public addTo(stage: Stage) {
	  if (this.tooltip) {
	    stage.addChild(this.tooltip.shape);
      stage.addChild(this.tooltip.nameText);
	  }
      stage.addChild(this.image);
  }
  public removeFrom(stage: Stage) {
	  if (this.tooltip) {
        stage.removeChild(this.tooltip.shape);
        stage.removeChild(this.tooltip.nameText);
	  }
      stage.removeChild(this.image);
  }
}
