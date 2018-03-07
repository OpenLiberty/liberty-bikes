import { Point } from "./point";

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
  containsPoint(other: Point) {
    const b1 = this.sign(other, this.point1, this.point2) < 0.0;
    const b2 = this.sign(other, this.point2, this.point3) < 0.0;
    const b3 = this.sign(other, this.point3, this.point1) < 0.0;

    return (b1 == b2) && (b2 == b3);
  }

  sign(p1: Point, p2: Point, p3: Point) {
    const term1 = p1.x - p3.x;
    const term2 = p2.y - p3.y;
    const term3 = p2.x - p3.x;
    const term4 = p1.y - p3.y;

    return term1 * term2 - term3 * term4;
  }

}
