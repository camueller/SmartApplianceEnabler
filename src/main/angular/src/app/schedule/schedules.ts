import {Schedule} from './schedule';

export class Schedules {
  '@class' = 'de.avanux.smartapplianceenabler.schedule.Schedules';
  schedules: Schedule[] = [];

  public constructor(init?: Partial<Schedules>) {
    Object.assign(this, init);
  }
}
