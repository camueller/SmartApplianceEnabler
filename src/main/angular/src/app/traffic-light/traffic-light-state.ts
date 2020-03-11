export interface TrafficLightState {

  isRed(): boolean;

  isYellow(): boolean;

  isGreen(): boolean;

  isGreenBlink(): boolean;
}
