import {MeterDefaults} from './meter-defaults';
import {Meter} from './meter';
import {S0ElectricityMeter} from './s0/s0-electricity-meter';

export class MeterTestdata {

  public static meterdefaults_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.meter.MeterDefaults',
      'httpElectricityMeter': {
        '@class': 'de.avanux.smartapplianceenabler.meter.HttpElectricityMeterDefaults',
        'factorToWatt': 1,
        'minPulseDuration': 20,
        'pollInterval': 10
      },
      'modbusElectricityMeter': {
        '@class': 'de.avanux.smartapplianceenabler.modbus.ModbusElectricityMeterDefaults',
        'minPulseDuration': 20,
        'pollInterval': 10
      },
      's0ElectricityMeter': {
        '@class': 'de.avanux.smartapplianceenabler.meter.S0ElectricityMeterDefaults',
        'minPulseDuration': 20
      }
    };
  }

  public static meterdefaults_type(): MeterDefaults {
    return new MeterDefaults({
      s0ElectricityMeterDefaults: {
        minPulseDuration: 20,
      },
      httpElectricityMeterDefaults: {
        factorToWatt: 1,
        pollInterval: 10,
      },
      modbusElectricityMeterDefaults: {
        pollInterval: 10,
      },
    });
  }

  public static none_type(): Meter {
    return new Meter();
  }

  public static none_undefinedtype_type(): Meter {
    return new Meter({
      type: undefined
    });
  }

  public static s0ElectricityMeter_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.meter.S0ElectricityMeter',
      'gpio': 1,
      'pinPullResistance': 'PULL_DOWN',
      'impulsesPerKwh': 1000,
      'minPulseDuration': 20
    };
  }

  public static s0ElectricityMeter_type(): Meter {
    return new Meter({
      type: 'de.avanux.smartapplianceenabler.meter.S0ElectricityMeter',
      s0ElectricityMeter: new S0ElectricityMeter({
        '@class': 'de.avanux.smartapplianceenabler.meter.S0ElectricityMeter',
        gpio: 1,
        pinPullResistance: 'PULL_DOWN',
        impulsesPerKwh: 1000,
        minPulseDuration: 20,
      })
    });
  }


}
