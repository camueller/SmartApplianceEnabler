export interface TrafficLightState {

  isRed(): boolean;

  isRedBlink(): boolean;

  isYellow(): boolean;

  isYellowBlink(): boolean;

  isGreen(): boolean;

  isGreenBlink(): boolean;
}
