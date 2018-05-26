import { Shape, Text, Container } from 'createjs-module';
import { Player } from './player';
import { Constants } from '../game/constants';
import { Tooltip } from './tooltip';

export class PlayerTooltip {
  static readonly tooltipDistance = Constants.BOX_SIZE * 5;

  public tooltipShape: Container;
  public lifetime = 30;

  showAbove: boolean;
  playerVertical: boolean;
  lastX = 0;
  lastY = 0; // track if player is moving up or down
  lastDirection = 'NONE';

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

    this.tooltipShape.regX = this.tooltipShape.getBounds().width / 2;
    this.tooltipShape.regY = this.tooltipShape.getBounds().height / 2;
  }

  public update(direction?: string) {
    if (this.player.x === this.lastX && this.player.y === this.lastY && direction === this.lastDirection) {
      // Nothing to update
      return;
    }
    const tooltipBounds = this.tooltipShape.getTransformedBounds();
    const playerBounds = this.player.image.getTransformedBounds();
    let verticalMargin = PlayerTooltip.tooltipDistance;

    // positioning
    let mimumRequiredDistance = this.player.image.getTransformedBounds().height / 2 + verticalMargin + tooltipBounds.height;
    if (this.player.y > Constants.BOARD_SIZE - mimumRequiredDistance) {
      this.showAbove = true;
    } else if (this.player.y <= (this.player.image.getTransformedBounds().height / 2 + verticalMargin + tooltipBounds.height)) {
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

    // The player sprite and tooltip are both registered at their center, rather than top left
    this.tooltipShape.x = this.player.image.x;
    if (this.showAbove) {
      this.tooltipShape.y = (this.player.image.y - (tooltipBounds.height / 2 + playerBounds.height / 2 + verticalMargin));
    } else {
      this.tooltipShape.y = (this.player.image.y + (tooltipBounds.height / 2 + playerBounds.height / 2 + verticalMargin));
    }

    // prevent tooltip from going off screen
    if (this.player.x < tooltipBounds.width / 2) {
      this.tooltipShape.x = (tooltipBounds.width / 2) - this.player.x;
    }

    if (this.player.x + (tooltipBounds.width / 2) > Constants.BOARD_SIZE) {
      this.tooltipShape.x = -(this.player.x + tooltipBounds.width / 2) + Constants.BOARD_SIZE;
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
