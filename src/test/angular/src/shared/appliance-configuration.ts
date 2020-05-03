import {Control} from '../../../../main/angular/src/app/control/control';
import {Meter} from '../../../../main/angular/src/app/meter/meter';
import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';
import {Schedule} from '../../../../main/angular/src/app/schedule/schedule';

export class ApplianceConfiguration {
  appliance: Appliance;
  meter: Meter;
  control: Control;
  controlTemplate?: string;
  schedules?: Schedule[];

  public constructor(init?: Partial<ApplianceConfiguration>) {
    Object.assign(this, init);
  }
}
