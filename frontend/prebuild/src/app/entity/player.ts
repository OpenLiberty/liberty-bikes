import { Bitmap, Container } from 'createjs-module';
import { PlayerTooltip } from './player.tooltip';
import { Assets } from '../game/assets';
import { Constants } from '../game/constants';

export class Player {
  displayObject: Container;

  get object() {
    return this.displayObject;
  }


  childrenLoaded = false;

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
    // console.log(`[Player-${this.name}]  x=${x}  y=${y}  direction=${direction}`);
    let playerMoved: boolean = this.x !== x || this.y !== y || this.direction !== direction;
    this.x = x;
    this.y = y;
    this.direction = direction;

    if (!this.childrenLoaded) {
      this.displayObject = new Container();

      this.tooltip = new PlayerTooltip(this);
      this.displayObject.addChild(this.tooltip.tooltipShape);

      switch (this.color.toLowerCase()) {
        case Constants.GREEN_COLOR:
          this.image = Assets.PLAYER_GREEN_BITMAP.clone();
          break;
        case Constants.BLUE_COLOR:
          this.image = Assets.PLAYER_BLUE_BITMAP.clone();
          break;
        case Constants.ORANGE_COLOR:
          this.image = Assets.PLAYER_ORANGE_BITMAP.clone();
          break;
        case Constants.PURPLE_COLOR:
          this.image = Assets.PLAYER_PURPLE_BITMAP.clone();
          break;
        default:
          console.warn("Player color did not match available images. Defaulting to green.");
          this.image = Assets.PLAYER_GREEN_BITMAP.clone();
          break;
      }
      this.image.scaleX = 5 / 6;
      this.image.scaleY = 5 / 6;
      // Set the center point of the player image to be the front 1/3 of the area
      this.image.regX = this.image.getBounds().width / 2;
      this.image.regY = this.image.getBounds().height / 3;
      this.displayObject.addChild(this.image);

      this.explosionImage = Assets.PLAYER_DEAD_BITMAP.clone();
      this.explosionImage.scaleX = 1 / 5;
      this.explosionImage.scaleY = 1 / 5;
      if (this.explosionImage.getBounds() !== null) {
          this.explosionImage.regX = this.explosionImage.getBounds().width / 2;
          this.explosionImage.regY = this.explosionImage.getBounds().height / 2;
      }
      this.childrenLoaded = true;
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

    this.displayObject.x = x;
    this.displayObject.y = y;
    this.tooltip.update(direction);

    return playerMoved;
  }

  public visible(isVisible: boolean) {
    if (this.childrenLoaded) {
      this.image.visible = isVisible;
      this.explosionImage.visible = isVisible;
      this.tooltip.visible(isVisible);
    }

  }

  public setStatus(status: string) {
    if (this.childrenLoaded && status === 'Dead' && this.status !== 'Dead') {
      this.explosionImage.x = this.image.x;
      this.explosionImage.y = this.image.y;
      this.displayObject.addChild(this.explosionImage);
    }
    this.status = status;
  }
}
