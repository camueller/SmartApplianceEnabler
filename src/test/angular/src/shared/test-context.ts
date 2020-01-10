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

  public static getConfiguration(t: TestController, configuationName: string,
                                 factory: () => ApplianceConfiguration): ApplianceConfiguration {
    const configurationKey = TestContext.configurationKey(t, configuationName);
    let configuration = TestContext.configurations.get(configurationKey);
    if (!configuration) {
      configuration = factory.call(TestContext);
      TestContext.configurations.set(configurationKey, configuration);
    }
    return configuration;
  }

  public static getHeatPump(t: TestController): ApplianceConfiguration {
    return TestContext.getConfiguration(t, 'HeatPump', () => TestContext.createHeatPump(t));
  }

  public static createHeatPump(t: TestController): ApplianceConfiguration {
    return new ApplianceConfiguration({
      appliance: {...heatPump, id: generateApplianceId()},
      meter: {type: S0ElectricityMeter.TYPE, s0ElectricityMeter: s0Meter},
      control: {type: Switch.TYPE, startingCurrentDetection: false, switch_}
    });
  }

  public static getWashingMachine(t: TestController): ApplianceConfiguration {
    return TestContext.getConfiguration(t, 'Washing Machine', () => TestContext.createWashingMachine(t));
  }

  public static createWashingMachine(t: TestController): ApplianceConfiguration {
    return new ApplianceConfiguration({
      appliance: {...washingMachine, id: generateApplianceId()},
      meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete},
      control: {type: Switch.TYPE, startingCurrentDetection: false, switch_}
    });
  }

  public static getFridge(t: TestController): ApplianceConfiguration {
    return TestContext.getConfiguration(t, 'Fridge', () => TestContext.createFridge(t));
  }

  public static createFridge(t: TestController): ApplianceConfiguration {
    return new ApplianceConfiguration({
      appliance: {...fridge, id: generateApplianceId()},
      meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete},
      control: {type: AlwaysOnSwitch.TYPE, startingCurrentDetection: false, alwaysOnSwitch}
    });
  }
}
