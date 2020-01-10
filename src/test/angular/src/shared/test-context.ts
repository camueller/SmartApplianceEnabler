import {ApplianceConfiguration} from './appliance-configuration';
import {heatPump} from '../fixture/appliance/heatpump';
import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';
import {generateApplianceId} from './appliance-id-generator';
import {Meter} from '../../../../main/angular/src/app/meter/meter';
import {switch_} from '../fixture/control/switch';
import {Control} from '../../../../main/angular/src/app/control/control';
import {washingMachine} from '../fixture/appliance/washingmachine';
import {s0Meter} from '../fixture/meter/s0-meter';
import {S0ElectricityMeter} from '../../../../main/angular/src/app/meter-s0/s0-electricity-meter';
import {Switch} from '../../../../main/angular/src/app/control-switch/switch';
import {HttpElectricityMeter} from '../../../../main/angular/src/app/meter-http/http-electricity-meter';
import {httpMeter_2HttpRead_complete} from '../fixture/meter/http-meter';
import {fridge} from '../fixture/appliance/fridge';
import {AlwaysOnSwitch} from '../../../../main/angular/src/app/control-alwayson/always-on-switch';
import {alwaysOnSwitch} from '../fixture/control/always-on-switch';

export class TestContext {

  private static configurations = new Map();

  public static configurationKey(t: TestController, configurationName: string) {
    return configurationName;
  }

  public static runnerConfigurationKey(t: TestController, configurationName: string) {
    return JSON.stringify({configurationName, userAgent: t.browser.name});
  }

  public static getHeatPump(t: TestController): ApplianceConfiguration {
    const configurationKey = TestContext.configurationKey(t, 'HeatPump');
    let configuration = TestContext.configurations.get(configurationKey);
    if (!configuration) {
      const appliance: Appliance = {...heatPump, id: generateApplianceId()};
      const meter: Meter = {type: S0ElectricityMeter.TYPE, s0ElectricityMeter: s0Meter};
      const control: Control = {type: Switch.TYPE, startingCurrentDetection: false, switch_};
      configuration = new ApplianceConfiguration({
        appliance,
        meter,
        control
      });
      TestContext.configurations.set(configurationKey, configuration);
    }
    return configuration;
  }

  public static getWashingMachine(t: TestController): ApplianceConfiguration {
    const configurationKey = TestContext.configurationKey(t, 'WashingMachine');
    let configuration = TestContext.configurations.get(configurationKey);
    if (!configuration) {
      const appliance: Appliance = {...washingMachine, id: generateApplianceId()};
      const meter: Meter = {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete};
      const control: Control = {type: Switch.TYPE, startingCurrentDetection: false, switch_};
      configuration = new ApplianceConfiguration({
        appliance,
        meter,
        control
      });
      TestContext.configurations.set(configurationKey, configuration);
    }
    return configuration;
  }

  public static getFridge(t: TestController): ApplianceConfiguration {
    const configurationKey = TestContext.configurationKey(t, 'Fridge');
    let configuration = TestContext.configurations.get(configurationKey);
    if (!configuration) {
      const appliance: Appliance = {...fridge, id: generateApplianceId()};
      const meter: Meter = {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete};
      const control: Control = {type: AlwaysOnSwitch.TYPE, startingCurrentDetection: false, alwaysOnSwitch};
      configuration = new ApplianceConfiguration({
        appliance,
        meter,
        control
      });
      TestContext.configurations.set(configurationKey, configuration);
    }
    return configuration;
  }
}
