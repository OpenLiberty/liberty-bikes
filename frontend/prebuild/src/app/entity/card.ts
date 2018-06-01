import { Container, DisplayObject, Shape, Tween, Ease } from "createjs-module";

export class Card {
  private readonly ACCENT_WIDTH = 10;
  private readonly MARGIN = 20;
  private readonly DURATION = 1000;

  object = new Container();

  get displayObject(): DisplayObject {
    return this.object;
  }

  width: number;
  height: number;

  background = new Shape();
  backgroundCommand: Object;

  accent = new Shape();
  accentCommand: Object;

  mask = new Shape();

  contents = new Container();

  get content(): Container {
    return this.contents;
  }

  get contentWidth(): number {
    return this.width - (this.ACCENT_WIDTH + this.MARGIN);
  }

  get contentHeight(): number {
    return this.height;
  }

  constructor(width: number, height: number, color = 'rgb(255, 255, 255)') {
    this.width = width;
    this.height = height;

    this.contents.setBounds(0, 0, this.contentWidth, this.contentHeight);

    this.accentCommand = this.accent.graphics.beginFill(color).rect(0, 0, this.ACCENT_WIDTH, 0).command;

    this.backgroundCommand = this.background.graphics.beginFill(color).rect(0, 0, 0, height).command;
    this.background.alpha = 0;

    this.mask.graphics.beginFill('black').rect(this.ACCENT_WIDTH, 0, width - this.ACCENT_WIDTH, height);

    // Start offscreen to the left
    this.contents.x = -(this.contents.getBounds().width);
    this.contents.y = 0
    this.contents.alpha = 0
    this.contents.mask = this.mask;

    this.object.addChild(this.background);
    this.object.addChild(this.accent);
    this.object.addChild(this.contents);
  }

  public show() {
    Tween.get(this.backgroundCommand).to({w: this.width}, this.DURATION, Ease.quadOut);
    Tween.get(this.background).to({alpha: 0.18}, this.DURATION, Ease.quadOut);

    Tween.get(this.accentCommand).to({h: this.height}, this.DURATION, Ease.quadOut);

    Tween.get(this.contents).to({alpha: 1}, this.DURATION, Ease.quadOut);
    Tween.get(this.contents).to({x: this.ACCENT_WIDTH + this.MARGIN}, this.DURATION, Ease.quadOut);
  }
}
