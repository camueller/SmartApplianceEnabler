import {EvCharger} from './ev-charger';

export class EvChargerTemplates {

  static getTemplates(): { [name: string]: EvCharger } {
    const templates: { [name: string]: EvCharger } = {};
    templates['PhoenixContact'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "voltage": null,\n' +
      '  "phases": null,\n' +
      '  "pollInterval": null,\n' +
      '  "startChargingStateDetectionDelay": 300,\n' +
      '  "forceInitialCharging": null,\n' +
      '  "vehicles": [],\n' +
      '  "modbusControl": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.modbus.EVModbusControl",\n' +
      '    "idref": "wallbox",\n' +
      '    "modbusReads": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusRead",\n' +
      '        "address": "100",\n' +
      '        "byteOrder": null,\n' +
      '        "bytes": null,\n' +
      '        "factorToValue": null,\n' +
      '        "readValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(A)",\n' +
      '            "name": "VehicleNotConnected"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(B)",\n' +
      '            "name": "VehicleConnected"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(C|D)",\n' +
      '            "name": "Charging"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(E|F)",\n' +
      '            "name": "Error"\n' +
      '          }\n' +
      '        ],\n' +
      '        "type": "InputString"\n' +
      '      }\n' +
      '    ],\n' +
      '    "modbusWrites": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWrite",\n' +
      '        "address": "400",\n' +
      '        "factorToValue": null,\n' +
      '        "type": "Coil",\n' +
      '        "writeValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWriteValue",\n' +
      '            "name": "StartCharging",\n' +
      '            "value": "1"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWriteValue",\n' +
      '            "name": "StopCharging",\n' +
      '            "value": "0"\n' +
      '          }\n' +
      '        ]\n' +
      '      },\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWrite",\n' +
      '        "address": "300",\n' +
      '        "factorToValue": null,\n' +
      '        "type": "Holding",\n' +
      '        "writeValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWriteValue",\n' +
      '            "name": "ChargingCurrent",\n' +
      '            "value": "0"\n' +
      '          }\n' +
      '        ]\n' +
      '      }\n' +
      '    ],\n' +
      '    "slaveAddress": 180\n' +
      '  }\n' +
      '}\n');
    templates['wallbe (neuer Controller)'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "voltage": null,\n' +
      '  "phases": null,\n' +
      '  "pollInterval": null,\n' +
      '  "startChargingStateDetectionDelay": 300,\n' +
      '  "forceInitialCharging": null,\n' +
      '  "vehicles": [],\n' +
      '  "modbusControl": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.modbus.EVModbusControl",\n' +
      '    "idref": "wallbox",\n' +
      '    "modbusReads": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusRead",\n' +
      '        "address": "100",\n' +
      '        "byteOrder": null,\n' +
      '        "bytes": null,\n' +
      '        "factorToValue": null,\n' +
      '        "readValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(A)",\n' +
      '            "name": "VehicleNotConnected"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(B)",\n' +
      '            "name": "VehicleConnected"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(C|D)",\n' +
      '            "name": "Charging"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(E|F)",\n' +
      '            "name": "Error"\n' +
      '          }\n' +
      '        ],\n' +
      '        "type": "InputString"\n' +
      '      }\n' +
      '    ],\n' +
      '    "modbusWrites": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWrite",\n' +
      '        "address": "400",\n' +
      '        "factorToValue": null,\n' +
      '        "type": "Coil",\n' +
      '        "writeValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWriteValue",\n' +
      '            "name": "StartCharging",\n' +
      '            "value": "1"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWriteValue",\n' +
      '            "name": "StopCharging",\n' +
      '            "value": "0"\n' +
      '          }\n' +
      '        ]\n' +
      '      },\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWrite",\n' +
      '        "address": "528",\n' +
      '        "factorToValue": 10,\n' +
      '        "type": "Holding",\n' +
      '        "writeValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWriteValue",\n' +
      '            "name": "ChargingCurrent",\n' +
      '            "value": "0"\n' +
      '          }\n' +
      '        ]\n' +
      '      }\n' +
      '    ],\n' +
      '    "slaveAddress": 255\n' +
      '  }\n' +
      '}\n');
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
