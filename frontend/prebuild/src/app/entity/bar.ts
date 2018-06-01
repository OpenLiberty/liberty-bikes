import { Container, DisplayObject, Shape, Tween, Ease } from "createjs-module";
import { Constants } from "../game/constants";

export class Bar {
  private readonly DURATION = 500;

  object = new Container();

  get displayObject(): DisplayObject {
    return this.object;
  }

  height: number;
  width = Constants.BOARD_SIZE;
  backgroundCommand: Object;
  backgroundMask = new Shape();
  background = new Container();
  contents = new Container();

  get contentContainer(): Container {
    return this.contents;
  }

  constructor(height: number, color?: string) {
    this.height = height;

    this.contents.x = -100;
    // this.contents.x = 0;
    this.contents.y = 0;

    this.backgroundCommand = this.backgroundMask.graphics
      .beginFill('black')
      .rect(0, 0, 0, this.height)
      .command;

    this.background.mask = this.backgroundMask;
    this.contents.mask = this.backgroundMask;

    const backgroundHeader = new Shape();
    backgroundHeader.graphics.beginFill('#33307e').rect(0, 0, this.width, this.height / 3);

    const backgroundBody = new Shape();
    backgroundBody.graphics.beginFill('#03004e').rect(0, this.height / 3, this.width, 2 * this.height / 3);

    this.background.addChild(backgroundHeader);
    this.background.addChild(backgroundBody);

    this.background.alpha = 0;
    this.contents.alpha = 0;

    this.object.addChild(this.background);
    this.object.addChild(this.contents);

    this.object.x = 0;
    this.object.y = Constants.BOARD_SIZE / 2 - height / 2;
  }

  show() {
    Tween.get(this.backgroundCommand).to({w: this.width}, this.DURATION, Ease.quadOut);
    Tween.get(this.background).to({alpha: 0.8}, this.DURATION / 2, Ease.linear);

    Tween.get(this.contents).to({alpha: 1}, this.DURATION, Ease.quadOut);
    Tween.get(this.contents).to({x: 0}, this.DURATION, Ease.quadOut);
  }
}
