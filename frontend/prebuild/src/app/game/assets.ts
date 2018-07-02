import { Bitmap } from 'createjs-module';

export class Assets {
  // According to EaselJS, "When a string path or image tag that is not yet loaded is used,
  // the stage may need to be redrawn before the Bitmap will be displayed."
  // For this reason, we need to pre-load an instance of the image (this copy never gets used)
  static readonly PLAYER_GREEN_BITMAP = new Bitmap('../../assets/images/bike_green.png');
  static readonly PLAYER_BLUE_BITMAP = new Bitmap('../../assets/images/bike_blue.png');
  static readonly PLAYER_ORANGE_BITMAP = new Bitmap('../../assets/images/bike_orange.png');
  static readonly PLAYER_PURPLE_BITMAP = new Bitmap('../../assets/images/bike_purple.png');
  static readonly PLAYER_DEAD_BITMAP = new Bitmap('../../assets/images/status_dead.png');
}
