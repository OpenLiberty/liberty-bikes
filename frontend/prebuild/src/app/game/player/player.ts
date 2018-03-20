export class Player {
  public name: string;
  public status: string;
  _color: string;

  constructor(name: string, status: string, color: string) {
    this.name = name;
    this.status = status;
    this._color = color;
  }

  get color(): string {
    if (this.status === 'Dead' || this.status === 'Disconnected') {
      return '#BBBBBB';
    }

    return this._color;
  }

  get style(): any {
    const style = {
      'border-left': `5px solid ${this.color}`,
      'background-color': `${this.backgroundColor(this.color)}`
    };

    return style;
  }

  backgroundColor(color: string): string {
    return `${color}30`;
  }
}
