import {Control} from '../../../../main/angular/src/app/control/control';
import {Meter} from '../../../../main/angular/src/app/meter/meter';
import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';

export class ApplianceConfiguration {
  appliance: Appliance;
  meter: Meter;
  control: Control;

  public constructor(init?: Partial<ApplianceConfiguration>) {
    Object.assign(this, init);
  }
}
