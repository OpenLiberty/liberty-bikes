import { Point } from './point';

export class Triangle {
  point1: Point;
  point2: Point;
  point3: Point;

  constructor(p1: Point, p2: Point, p3: Point) {
    this.point1 = p1;
    this.point2 = p2;
    this.point3 = p3;
  }

  // Adapted from https://www.gamedev.net/forums/topic/295943-is-this-a-better-point-in-triangle-test-2d/?do=findComment&comment=2874043
  // Should tolerate either vertex winding direction
  containsPoint(other: Point): boolean {
    const b1 = this.sign(other, this.point1, this.point2) < 0.0;
    const b2 = this.sign(other, this.point2, this.point3) < 0.0;
    const b3 = this.sign(other, this.point3, this.point1) < 0.0;

    return (b1 === b2) && (b2 === b3);
  }

  sign(p1: Point, p2: Point, p3: Point): number {
    const term1 = p1.x - p3.x;
    const term2 = p2.y - p3.y;
    const term3 = p2.x - p3.x;
    const term4 = p1.y - p3.y;

    return term1 * term2 - term3 * term4;
  }

  scale(factor: number): Triangle {
    const center = new Point((this.point1.x + this.point2.x + this.point3.x) / 3.0,
                             (this.point1.y + this.point2.y + this.point3.y) / 3.0);

    // New points centered on origin
    const newPoint1 = new Point(this.point1.x - center.x, this.point1.y - center.y);
    const newPoint2 = new Point(this.point2.x - center.x, this.point2.y - center.y);
    const newPoint3 = new Point(this.point3.x - center.x, this.point3.y - center.y);

    // Scale about the origin
    newPoint1.x = newPoint1.x * factor;
    newPoint1.y = newPoint1.y * factor;

    newPoint2.x = newPoint2.x * factor;
    newPoint2.y = newPoint2.y * factor;

    newPoint3.x = newPoint3.x * factor;
    newPoint3.y = newPoint3.y * factor;

    // Move back to original center
    newPoint1.x = newPoint1.x + center.x;
    newPoint1.y = newPoint1.y + center.y;

    newPoint2.x = newPoint2.x + center.x;
    newPoint2.y = newPoint2.y + center.y;

    newPoint3.x = newPoint3.x + center.x;
    newPoint3.y = newPoint3.y + center.y;

    return new Triangle(newPoint1, newPoint2, newPoint3);
  }

  // Angle in radians
  rotate(angle: number) {
    const center = new Point((this.point1.x + this.point2.x + this.point3.x) / 3.0,
                             (this.point1.y + this.point2.y + this.point3.y) / 3.0);

    // New points centered on origin
    const newPoint1 = new Point(this.point1.x - center.x, this.point1.y - center.y);
    const newPoint2 = new Point(this.point2.x - center.x, this.point2.y - center.y);
    const newPoint3 = new Point(this.point3.x - center.x, this.point3.y - center.y);

    // Rotate about origin
    newPoint1.x = newPoint1.x * Math.cos(angle) - newPoint1.y * Math.sin(angle);
    newPoint1.y = newPoint1.y * Math.cos(angle) - newPoint1.x * Math.sin(angle);

    newPoint2.x = newPoint2.x * Math.cos(angle) - newPoint2.y * Math.sin(angle);
    newPoint2.y = newPoint2.y * Math.cos(angle) - newPoint2.x * Math.sin(angle);

    newPoint3.x = newPoint3.x * Math.cos(angle) - newPoint3.y * Math.sin(angle);
    newPoint3.y = newPoint3.y * Math.cos(angle) - newPoint3.x * Math.sin(angle);

    // Move back to location
    newPoint1.x = newPoint1.x + center.x;
    newPoint1.y = newPoint1.y + center.y;

    newPoint2.x = newPoint2.x + center.x;
    newPoint2.y = newPoint2.y + center.y;

    newPoint3.x = newPoint3.x + center.x;
    newPoint3.y = newPoint3.y + center.y;

    return new Triangle(newPoint1, newPoint2, newPoint3);
  }

}
