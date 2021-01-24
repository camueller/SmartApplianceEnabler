export interface TrafficLightState {

  isRed(): boolean;

  isYellow(): boolean;

  isYellowBlink(): boolean;

  isGreen(): boolean;

  isGreenBlink(): boolean;
}
