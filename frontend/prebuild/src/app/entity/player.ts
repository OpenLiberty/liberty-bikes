import { Shape, Bitmap, Stage } from 'createjs-module';
import { PlayerTooltip } from './player.tooltip';
import { Assets } from '../game/assets';

export class Player {
  static audioLoaded: boolean = false;

  public name: string;
  public status: string;
  public color: string;
  public x: number;
  public y: number;
  public image: Bitmap;
  public explosionImage: Bitmap;
  public tooltip: PlayerTooltip;
  direction: string;

  public update(x: number, y: number, direction: string) {
    //console.log(`[Player-${this.name}]  x=${x}  y=${y}  direction=${direction}`);
    let playerMoved: boolean = this.x !== x || this.y !== y || this.direction !== direction;
    this.x = x;
    this.y = y;
    this.direction = direction;

    if (!this.tooltip) {
      this.tooltip = new PlayerTooltip(this);
    }

    if (!this.image) {
      this.image = Assets.PLAYER_BITMAP.clone();
      this.image.regX = this.image.getBounds().width / 2;
      this.image.regY = this.image.getBounds().height / 2;
      this.image.scaleX = 2.0;
      this.image.scaleY = 2.0;
    }

    if (direction === 'UP') {
      this.image.rotation = 0;
    } else if (direction === 'RIGHT') {
      this.image.rotation = 90;
    } else if (direction === 'DOWN') {
      this.image.rotation = 180;
    } else {
      this.image.rotation = 270;
    }

    this.image.x = x;
    this.image.y = y;
    this.tooltip.update(direction);

    return playerMoved;
  }

  public visible(isVisible: boolean) {
    this.image.visible = isVisible;
    this.explosionImage.visible = isVisible;
    this.tooltip.visible(isVisible);
  }

  public setStatus(status: string) {
    if (status === 'Dead' && this.status !== 'Dead' && !this.explosionImage) {
      this.explosionImage = Assets.PLAYER_DEAD_BITMAP.clone();
      this.explosionImage.scaleX = 1.2;
      this.explosionImage.scaleY = 1.2;
      this.explosionImage.x = this.x - this.explosionImage.getBounds().width / 2;
      this.explosionImage.y = this.y - this.explosionImage.getBounds().height / 2;
      if (!Player.audioLoaded) {
        Player.audioLoaded = true;
        Assets.BAM.load();
      }
      Assets.BAM.play();
    }
    this.status = status;
  }

  public addTo(stage: Stage) {
    if (this.tooltip) {
      stage.addChild(this.tooltip.tooltipShape);
    }

    stage.addChild(this.image);

    if (this.explosionImage) {
      stage.addChild(this.explosionImage);
    }
  }

  public removeFrom(stage: Stage) {
    if (this.tooltip) {
      stage.removeChild(this.tooltip.tooltipShape);
    }

    stage.removeChild(this.image);

    if (this.explosionImage) {
      stage.removeChild(this.explosionImage);
    }
  }
}
