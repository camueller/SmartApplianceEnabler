import {EvCharger} from './ev-charger';

export class EvChargerTemplates {

  static getTemplates(): { [name: string]: EvCharger } {
    const templates: { [name: string]: EvCharger } = {};
    templates['PhoenixContact'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "control": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.modbus.EVModbusControl",\n' +
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
      '    "@class": "de.avanux.smartapplianceenabler.modbus.EVModbusControl",\n' +
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
    templates['go-eCharger'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "voltage": null,\n' +
      '  "phases": null,\n' +
      '  "pollInterval": null,\n' +
      '  "startChargingStateDetectionDelay": 15,\n' +
      '  "forceInitialCharging": null,\n' +
      '  "vehicles": [],\n' +
      '  "httpControl": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.http.EVHttpControl",\n' +
      '    "contentProtocol": "json",\n' +
      '    "httpConfiguration": null,\n' +
      '    "httpReads": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.http.HttpRead",\n' +
      '        "readValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.http.HttpReadValue",\n' +
      '            "data": null,\n' +
      '            "extractionRegex": "(1)",\n' +
      '            "factorToValue": null,\n' +
      '            "name": "VehicleNotConnected",\n' +
      '            "path": "$.car"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.http.HttpReadValue",\n' +
      '            "data": null,\n' +
      '            "extractionRegex": "(3|4)",\n' +
      '            "factorToValue": null,\n' +
      '            "name": "VehicleConnected",\n' +
      '            "path": "$.car"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.http.HttpReadValue",\n' +
      '            "data": null,\n' +
      '            "extractionRegex": "(2)",\n' +
      '            "factorToValue": null,\n' +
      '            "name": "Charging",\n' +
      '            "path": "$.car"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.http.HttpReadValue",\n' +
      '            "data": null,\n' +
      '            "extractionRegex": "([^0])",\n' +
      '            "factorToValue": null,\n' +
      '            "name": "Error",\n' +
      '            "path": "$.err"\n' +
      '          }\n' +
      '        ],\n' +
      '        "url": "http://192.168.1.1/status"\n' +
      '      }\n' +
      '    ],\n' +
      '    "httpWrites": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.http.HttpWrite",\n' +
      '        "url": "http://192.168.1.1/mqtt?payload=",\n' +
      '        "writeValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.http.HttpWriteValue",\n' +
      '            "factorToValue": null,\n' +
      '            "method": "GET",\n' +
      '            "name": "ChargingCurrent",\n' +
      '            "value": "amp={0}"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.http.HttpWriteValue",\n' +
      '            "factorToValue": null,\n' +
      '            "method": "GET",\n' +
      '            "name": "StartCharging",\n' +
      '            "value": "alw=1"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.http.HttpWriteValue",\n' +
      '            "factorToValue": null,\n' +
      '            "method": "GET",\n' +
      '            "name": "StopCharging",\n' +
      '            "value": "alw=0"\n' +
      '          }\n' +
      '        ]\n' +
      '      }\n' +
      '    ]\n' +
      '  }\n' +
      '}');
    return templates;
  }
}
