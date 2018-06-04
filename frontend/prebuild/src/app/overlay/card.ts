import { Container, DisplayObject, Shape, Tween, Ease, Text, ColorFilter } from "createjs-module";

export class Card {
  private static readonly ACCENT_WIDTH = 10;
  private static readonly MARGIN = 20;
  private static readonly DURATION = 500;

  private static readonly HEADER_TEXT_FONT = 'small-caps 36px -apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
  private static readonly HEADER_TEXT_COLOR = 'rgba(255, 255, 255, 1)';

  private static readonly BODY_TEXT_FONT = '64px "Avenir-Black", "Arial Black", "Roboto Black", sans-serif';
  private static readonly BODY_TEXT_COLOR = 'rgba(255, 255, 255, 1)';

  private static readonly NORMAL_SCALE = 1.0;
  private static readonly EMPHASIS_SCALE = 1.5;

  private object = new Container();

  get displayObject(): DisplayObject {
    return this.object;
  }

  width: number;

  get height(): number {
    return Card.MARGIN
      + this.headerText.getBounds().height
      + this.bodyText.getBounds().height
      + Card.MARGIN;
  }

  center: boolean;

  background = new Shape();
  backgroundCommand: Object;

  colorLayer = new Shape();
  colorLayerCommand: Object;

  accent = new Shape();
  accentCommand: Object;

  mask = new Shape();

  private headerText = new Text();

  get headerString(): string {
    return this.headerText.text;
  }

  set headerString(newString: string) {
    this.headerText.text = newString.toLocaleLowerCase();
  }

  private bodyText = new Text();

  get bodyString(): string {
    return this.bodyText.text;
  }

  set bodyString(newString: string) {
    this.bodyText.text = newString;
    let scale = Card.NORMAL_SCALE;

    if (newString.length > 10) {
      // Scale text down to fit
      scale = scale / 1.5;
    }

    this.bodyText.scaleX = scale;
    this.bodyText.scaleY = scale;

    if (this.emphasize) {
      Tween.get(this.bodyText).to({scaleX: Card.EMPHASIS_SCALE, scaleY: Card.EMPHASIS_SCALE}).to({scaleX: scale, scaleY: scale}, 250);
    }
  }

  private emphasize = false;

  get emphasizeBody(): boolean {
    return this.emphasize;
  }

  set emphasizeBody(emp: boolean) {
    this.emphasize = emp;
  }

  constructor(width: number, header: string, body: string,  centerHorizontal = false, color = 'rgba(255, 255, 255, 1)') {
    this.width = width;
    this.center = centerHorizontal;

    // Start offscreen to the left
    if (this.center) {
      this.headerText.textAlign = 'center';
    }

    this.headerString = header;
    this.headerText.font = Card.HEADER_TEXT_FONT;
    this.headerText.color = Card.HEADER_TEXT_COLOR;
    this.headerText.x = -(this.headerText.getBounds().width);
    this.headerText.y = Card.MARGIN;
    this.headerText.alpha = 0
    this.headerText.mask = this.mask;

    if (this.center) {
      this.bodyText.textAlign = 'center';
    }

    this.bodyString = body;
    this.bodyText.font = Card.BODY_TEXT_FONT;
    this.bodyText.color = Card.BODY_TEXT_COLOR;
    this.bodyText.x = -(this.bodyText.getBounds().width);
    this.bodyText.y = this.headerText.y + this.headerText.getBounds().height;

    this.bodyText.alpha = 0
    this.bodyText.mask = this.mask;

    this.accentCommand = this.accent.graphics.beginFill(color).rect(0, 0, Card.ACCENT_WIDTH, 0).command;

    this.backgroundCommand = this.background.graphics.beginFill('black').rect(0, 0, 0, this.height).command;
    this.background.alpha = 0;

    this.colorLayerCommand = this.colorLayer.graphics.beginFill(color).rect(0, 0, 0, this.height).command;
    this.colorLayer.alpha = 0;

    this.mask.graphics.beginFill('black').rect(Card.ACCENT_WIDTH, 0, width - Card.ACCENT_WIDTH, this.height);

    this.object.addChild(this.background);
    this.object.addChild(this.colorLayer);
    this.object.addChild(this.accent);
    this.object.addChild(this.headerText);
    this.object.addChild(this.bodyText);
  }

  public show() {
    Tween.get(this.backgroundCommand).to({w: this.width}, Card.DURATION, Ease.quadOut);
    Tween.get(this.background).to({alpha: 0.9}, Card.DURATION, Ease.quadOut);

    Tween.get(this.colorLayerCommand).to({w: this.width}, Card.DURATION, Ease.quadOut);
    Tween.get(this.colorLayer).to({alpha: 0.18}, Card.DURATION, Ease.quadOut);

    Tween.get(this.accentCommand).to({h: this.height}, Card.DURATION, Ease.quadOut);

    Tween.get(this.headerText).to({alpha: 1}, Card.DURATION, Ease.quadOut);

    // Setting textalign to center effectively registers the text at its center point
    // so no adjustment for the width of the text is needed.
    const headerX = this.center
      ? ((this.width - Card.ACCENT_WIDTH) / 2) + Card.ACCENT_WIDTH
      : Card.ACCENT_WIDTH + Card.MARGIN;
    Tween.get(this.headerText).to({x: headerX}, Card.DURATION, Ease.quadOut);

    Tween.get(this.bodyText).to({alpha: 1}, Card.DURATION, Ease.quadOut);

    // Setting textalign to center effectively registers the text at its center point
    // so no adjustment for the width of the text is needed.
    const bodyX = this.center
      ? ((this.width - Card.ACCENT_WIDTH) / 2) + Card.ACCENT_WIDTH
      : Card.ACCENT_WIDTH + Card.MARGIN;
    Tween.get(this.bodyText).to({x: bodyX}, Card.DURATION, Ease.quadOut);
  }

  public hide(delay = 0) {
    Tween.get(this.backgroundCommand).wait(delay).to({w: 0}, Card.DURATION, Ease.quadOut);
    Tween.get(this.background).wait(delay).to({alpha: 0}, Card.DURATION, Ease.quadOut);

    Tween.get(this.colorLayerCommand).wait(delay).to({w: 0}, Card.DURATION, Ease.quadOut);
    Tween.get(this.colorLayer).wait(delay).to({alpha: 0}, Card.DURATION, Ease.quadOut);

    Tween.get(this.accentCommand).wait(delay).to({h: 0}, Card.DURATION, Ease.quadOut);
    Tween.get(this.accent).wait(delay).to({y: this.height}, Card.DURATION, Ease.quadOut);

    Tween.get(this.headerText).wait(delay).to({alpha: 0}, Card.DURATION, Ease.quadOut);
    Tween.get(this.headerText).wait(delay).to({x: -this.width}, Card.DURATION, Ease.quadOut);

    Tween.get(this.bodyText).wait(delay).to({alpha: 0}, Card.DURATION, Ease.quadOut);
    Tween.get(this.bodyText).wait(delay).to({x: -this.width}, Card.DURATION, Ease.quadOut);
  }
}
