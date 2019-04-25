import {EvCharger} from './ev-charger';

export class EvChargerTemplates {

  static getTemplates(): { [name: string]: EvCharger } {
    const templates: { [name: string]: EvCharger } = {};
    templates['PhoenixContact'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "control": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.control.ev.EVModbusControl",\n' +
      '    "slaveAddress": 180,\n' +
      '    "configuration": [\n' +
      '      {\n' +
      '        "name": "VehicleNotConnected",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(A)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "VehicleConnected",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(B)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "Charging",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(C|D)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "ChargingCompleted",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(B)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "Error",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(E|F)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "ChargingCompleted",\n' +
      '        "address": "204",\n' +
      '        "type": "Discrete",\n' +
      '        "extractionRegex": null,\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "StartCharging",\n' +
      '        "address": "400",\n' +
      '        "type": "Coil",\n' +
      '        "value": "1",\n' +
      '        "write": true\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "StopCharging",\n' +
      '        "address": "400",\n' +
      '        "type": "Coil",\n' +
      '        "value": "0",\n' +
      '        "write": true\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "ChargingCurrent",\n' +
      '        "address": "300",\n' +
      '        "type": "Holding",\n' +
      '        "write": true\n' +
      '      }\n' +
      '    ]\n' +
      '  }\n' +
      '}');
    templates['wallbe (neuer Controller)'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "control": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.control.ev.EVModbusControl",\n' +
      '    "slaveAddress": 255,\n' +
      '    "configuration": [\n' +
      '      {\n' +
      '        "name": "VehicleNotConnected",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(A)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "VehicleConnected",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(B)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "Charging",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(C|D)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "ChargingCompleted",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(B)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "Error",\n' +
      '        "address": "100",\n' +
      '        "type": "InputString",\n' +
      '        "extractionRegex": "(E|F)",\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "ChargingCompleted",\n' +
      '        "address": "204",\n' +
      '        "type": "Discrete",\n' +
      '        "extractionRegex": null,\n' +
      '        "write": false\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "StartCharging",\n' +
      '        "address": "400",\n' +
      '        "type": "Coil",\n' +
      '        "value": "1",\n' +
      '        "write": true\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "StopCharging",\n' +
      '        "address": "400",\n' +
      '        "type": "Coil",\n' +
      '        "value": "0",\n' +
      '        "write": true\n' +
      '      },\n' +
      '      {\n' +
      '        "name": "ChargingCurrent",\n' +
      '        "address": "528",\n' +
      '        "type": "Holding",\n' +
      '        "write": true,\n' +
      '        "factorToValue": "10"\n' +
      '      }\n' +
      '    ]\n' +
      '  }\n' +
      '}');
    return templates;
  }
}
