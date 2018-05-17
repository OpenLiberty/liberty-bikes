import { Shape, Text, Container } from 'createjs-module';
import { Player } from './player';
import { Constants } from '../game/constants';
import { Tooltip } from './tooltip';

export class PlayerTooltip {
  static readonly tooltipDistance = Constants.BOX_SIZE * 3;

  public tooltipShape: Container;
  public lifetime: number = 30;

  showAbove: boolean;
  playerVertical: boolean;
  lastX: number = 0;
  lastY: number = 0; // track if player is moving up or down
  lastDirection: string = 'NONE';

  constructor(public player: Player) {
    if (!this.player.color) {
      console.log('WARNING: Creating a tooltip with a player that is not properly initialized');
    }
    this.tooltipShape = new Tooltip(
      this.player.name,
      '28px -apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif',
      '#000',
      this.player.color,
      230
    ).object;
  }

  public update(direction?: string) {
    if (this.player.x === this.lastX && this.player.y === this.lastY && direction === this.lastDirection) {
      // Nothing to update
      return;
    }
    const tooltipBounds = this.tooltipShape.getBounds();
    const playerBounds = this.player.image.getBounds();
    let verticalMargin = PlayerTooltip.tooltipDistance * 2;

    // positioning
    // If label positioning becomes misaligned again, rewrite this to use the player size etc
    // rather than magic constants.

    if (this.player.y > Constants.BOARD_SIZE - (tooltipBounds.height + verticalMargin + playerBounds.height)) {
      this.showAbove = true;
    } else if (this.player.y <= (tooltipBounds.height + verticalMargin)) {
      this.showAbove = false;
    } else if (this.player.y < this.lastY) {
      this.showAbove = false;
    } else if (this.player.y > this.lastY) {
      this.showAbove = true;
    }

    if (direction === 'UP' || direction === 'DOWN') {
      this.playerVertical = true;
    } else if (direction === 'LEFT' || direction === 'RIGHT') {
      this.playerVertical = false;
    }

    this.lastX = this.player.x;
    this.lastY = this.player.y;

    if (this.playerVertical) {
      this.tooltipShape.x = (this.player.x - tooltipBounds.width / 2) + playerBounds.width / 2;
      if (this.showAbove) {
        this.tooltipShape.y = (this.player.y - tooltipBounds.height) - verticalMargin;
      } else {
        this.tooltipShape.y = (this.player.y + playerBounds.height) + verticalMargin;
      }
    } else {
      // The image rotates, but the bounds don't, so swap width and height for the player bounds
      this.tooltipShape.x = (this.player.x - tooltipBounds.width / 2);
      if (this.showAbove) {
        this.tooltipShape.y = (this.player.y - tooltipBounds.height) - verticalMargin;
      } else {
        this.tooltipShape.y = (this.player.y + playerBounds.height) + verticalMargin;
      }
    }

    // prevent tooltip from going off screen
    if (this.tooltipShape.x < 0) {
      this.tooltipShape.x = 0;
    }

    if (this.tooltipShape.x + tooltipBounds.width > Constants.BOARD_SIZE) {
      this.tooltipShape.x = Constants.BOARD_SIZE - tooltipBounds.width;
    }

    // start fading out after 15 ticks
    if (this.player.status === 'Alive') {
      if (this.lifetime > 0) {
        this.lifetime--;
      }

      if (this.lifetime < 15) {
        this.alpha = this.lifetime / 15;
      }
    } else {
      this.alpha = 0.9;
    }
  }

  public visible(isVisible: boolean) {
    this.tooltipShape.visible = isVisible;
  }

  set alpha(newAlpha: number) {
    if (newAlpha > 0.9) {
      newAlpha = 0.9;
    }
    this.tooltipShape.alpha = newAlpha;
  }
}
