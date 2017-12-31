import {Settings} from './settings';
import {SettingsDefaults} from './settings-defaults';

export class SettingsTestdata {

  public static settingsdefaults_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.webservice.SettingsDefaults',
      'holidaysUrl': 'http://feiertage.jarmedia.de/api/?jahr={0}&nur_land=HE',
      'modbusTcpHost': '127.0.0.1',
      'modbusTcpPort': 502,
      'pulseReceiverPort': 9999
    };
  }

  public static settingsdefaults_type(): SettingsDefaults {
    return new SettingsDefaults({
      'holidaysUrl': 'http://feiertage.jarmedia.de/api/?jahr={0}&nur_land=HE',
      'modbusTcpHost': '127.0.0.1',
      'modbusTcpPort': 502,
      'pulseReceiverPort': 9999
    });
  }

  public static none_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.webservice.Settings',
      'holidaysEnabled': false,
      'holidaysUrl': null,
      'modbusEnabled': false,
      'modbusTcpHost': null,
      'modbusTcpPort': null,
      'pulseReceiverEnabled': false,
      'pulseReceiverPort': null
    };
  }

  public static none_type(): Settings {
    return new Settings({
      'holidaysEnabled': false,
      'holidaysUrl': null,
      'modbusEnabled': false,
      'modbusTcpHost': null,
      'modbusTcpPort': null,
      'pulseReceiverEnabled': false,
      'pulseReceiverPort': null
    });
  }

  public static all_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.webservice.Settings',
      'holidaysEnabled': true,
      'holidaysUrl': 'http://service.domain.de/path',
      'modbusEnabled': true,
      'modbusTcpHost': 'modbushost',
      'modbusTcpPort': 1234,
      'pulseReceiverEnabled': true,
      'pulseReceiverPort': 9876
    };
  }

  public static all_type(): Settings {
    return new Settings({
      'holidaysEnabled': true,
      'holidaysUrl': 'http://service.domain.de/path',
      'modbusEnabled': true,
      'modbusTcpHost': 'modbushost',
      'modbusTcpPort': 1234,
      'pulseReceiverEnabled': true,
      'pulseReceiverPort': 9876
    });
  }

}
