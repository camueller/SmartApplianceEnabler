import {Control} from './control';
import {ControlDefaults} from './control-defaults';
import {Switch} from './switch/switch';
import {StartingCurrentSwitch} from './startingcurrent/starting-current-switch';

export class ControlTestdata {

  public static controldefaults_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.control.ControlDefaults',
      'startingCurrentSwitchDefaults': {
        '@class': 'de.avanux.smartapplianceenabler.control.StartingCurrentSwitchDefaults',
        'finishedCurrentDetectionDuration': 300,
        'minRunningTime': 600,
        'powerThreshold': 15,
        'startingCurrentDetectionDuration': 30
      },
      'electricVehicleChargerDefaults': {
        'voltage': 230,
        'phases': 1,
        'chargeLoss': 10,
        'pollInterval': 10,
        'startChargingStateDetectionDelay': 300,
        'forceInitialCharging': false,
      }
    };
  }

  public static controldefaults_type(): ControlDefaults {
    return new ControlDefaults();
  }

  public static none_type(): Control {
    return new Control();
  }

  public static none_undefinedtype_type(): Control {
    return new Control({
      type: undefined
    });
  }

  public static switch_json(): any {
    return {
        '@class': 'de.avanux.smartapplianceenabler.control.Switch',
        'gpio': 1
      };
  }

  public static switch_type(): Control {
    return new Control({
      type: 'de.avanux.smartapplianceenabler.control.Switch',
      switch_: new Switch({
        gpio: 1,
        reverseStates: undefined
      })
    });
  }

  public static switch_StartingCurrent_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.control.StartingCurrentSwitch',
      'control': {
        '@class': 'de.avanux.smartapplianceenabler.control.Switch',
        'gpio': 1,
        'pinPullResistance': null,
        'reverseStates': false
      },
      'dayTimeframeCondition': null,
      'finishedCurrentDetectionDuration': null,
      'minRunningTime': null,
      'powerThreshold': null,
      'startingCurrentDetectionDuration': null
    };
  }

  public static switch_StartingCurrent_type(): Control {
    return new Control({
      type: 'de.avanux.smartapplianceenabler.control.Switch',
      startingCurrentDetection: true,
      startingCurrentSwitch: new StartingCurrentSwitch({
        '@class': 'de.avanux.smartapplianceenabler.control.StartingCurrentSwitch',
        powerThreshold: null,
        startingCurrentDetectionDuration: null,
        finishedCurrentDetectionDuration: null,
        minRunningTime: null
      }),
      switch_: new Switch({
        '@class': 'de.avanux.smartapplianceenabler.control.Switch',
        gpio: 1,
        reverseStates: false
      })
    });
  }
}
