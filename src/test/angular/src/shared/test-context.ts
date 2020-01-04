import {ApplianceConfiguration} from './appliance-configuration';
import {heatPump} from '../fixture/appliance/heatpump';
import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';
import {generateApplianceId} from './appliance-id-generator';
import {Meter} from '../../../../main/angular/src/app/meter/meter';
import {switch_} from '../fixture/control/Switch';
import {Control} from '../../../../main/angular/src/app/control/control';
import {washingMachine} from '../fixture/appliance/washingmachine';
import {s0Meter} from '../fixture/meter/s0-meter';
import {S0ElectricityMeter} from '../../../../main/angular/src/app/meter-s0/s0-electricity-meter';
import {Switch} from '../../../../main/angular/src/app/control-switch/switch';
import {HttpElectricityMeter} from '../../../../main/angular/src/app/meter-http/http-electricity-meter';
import {httpMeter} from '../fixture/meter/http-meter';

export class TestContext {

  private static heatPump: ApplianceConfiguration;
  private static washingMachine: ApplianceConfiguration;

  public static getHeatPump(): ApplianceConfiguration {
    if (!TestContext.heatPump) {
      const appliance: Appliance = {...heatPump, id: generateApplianceId()};
      const meter: Meter = {type: S0ElectricityMeter.TYPE, s0ElectricityMeter: s0Meter};
      const control: Control = {type: Switch.TYPE, startingCurrentDetection: false, switch_};
      TestContext.heatPump = new ApplianceConfiguration({
        appliance,
        meter,
        control
      });
    }
    return TestContext.heatPump;
  }

  public static getWashingMachine(): ApplianceConfiguration {
    if (!TestContext.washingMachine) {
      const appliance: Appliance = {...washingMachine, id: generateApplianceId()};
      const meter: Meter = {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter};
      const control: Control = {type: Switch.TYPE, startingCurrentDetection: false, switch_};
      TestContext.washingMachine = new ApplianceConfiguration({
        appliance,
        meter,
        control
      });
    }
    return TestContext.washingMachine;
  }
}
