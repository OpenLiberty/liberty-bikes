import { Text, Shape, Container } from 'createjs-module';

export class Tooltip {
  displayObject: Container;

  get object() {
    return this.displayObject;
  }

  label: Text;
  background: Shape;

  constructor(text: string, font: string, textColor: string, backgroundColor: string, minWidth?: number, minHeight?: number) {
    let paddingx = 20;
    let paddingy = 8;

    this.label = new Text(text, font, textColor);

    const labelBounds = this.label.getBounds();
    this.label.setBounds(labelBounds.x, labelBounds.y, Math.ceil(labelBounds.width), Math.ceil(labelBounds.height));

    if (minWidth && labelBounds.width + (paddingx * 2) < minWidth) {
      paddingx = Math.ceil((minWidth - labelBounds.width) / 2);
    }

    if (minHeight && labelBounds.height + (paddingy * 2) < minHeight) {
      paddingy = Math.ceil((minHeight - labelBounds.height) / 2);
    }

    this.label.x += paddingx;
    this.label.y += paddingy;

    this.background = new Shape();

    this.background.graphics.beginFill(backgroundColor).drawRoundRect(
      0,
      0,
      this.label.getBounds().width + (2 * paddingx),
      this.label.getBounds().height + (2 * paddingy),
      10
    );

    this.background.setBounds(0, 0, this.label.getBounds().width + (2 * paddingx), this.label.getBounds().height + (2 * paddingy));

    this.displayObject = new Container();
    this.displayObject.addChild(this.background);
    this.displayObject.addChild(this.label);
  }

}
