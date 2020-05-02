import {StartingCurrentSwitch} from '../../../../../main/angular/src/app/control/startingcurrent/starting-current-switch';

export const startingCurrentSwitch = new StartingCurrentSwitch({
  powerThreshold: 25,
  startingCurrentDetectionDuration: 300,
  finishedCurrentDetectionDuration: 600,
  minRunningTime: 120,
});
