import {EvCharger} from './ev-charger';

export class EvChargerTemplates {

  static getTemplates(): { [name: string]: EvCharger } {
    const templates: { [name: string]: EvCharger } = {};
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
      '    "contentProtocol": "JSON",\n' +
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
      '            "value": "amx={0}"\n' +
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
    templates['Keba P30 c-series >3.10.16 / x-series >1.11'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "voltage": null,\n' +
      '  "phases": null,\n' +
      '  "pollInterval": null,\n' +
      '  "startChargingStateDetectionDelay": 60,\n' +
      '  "forceInitialCharging": null,\n' +
      '  "vehicles": [],\n' +
      '  "modbusControl": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.modbus.EVModbusControl",\n' +
      '    "modbusReads": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusRead",\n' +
      '        "address": "1000",\n' +
      '        "type": "Holding",\n' +
      '        "valueType": "Integer",\n' +
      '        "byteOrder": null,\n' +
      '        "words": null,\n' +
      '        "factorToValue": null,\n' +
      '        "readValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(2|5)",\n' +
      '            "name": "VehicleConnected"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(3)",\n' +
      '            "name": "Charging"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(4)",\n' +
      '            "name": "Error"\n' +
      '          }\n' +
      '        ]\n' +
      '      },\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusRead",\n' +
      '        "address": "1004",\n' +
      '        "type": "Holding",\n' +
      '        "valueType": "Integer",\n' +
      '        "byteOrder": null,\n' +
      '        "words": null,\n' +
      '        "factorToValue": null,\n' +
      '        "readValues": [\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(0|1|3)",\n' +
      '            "name": "VehicleNotConnected"\n' +
      '          },\n' +
      '          {\n' +
      '            "@class": "de.avanux.smartapplianceenabler.modbus.ModbusReadValue",\n' +
      '            "extractionRegex": "(7|5)",\n' +
      '            "name": "VehicleConnected"\n' +
      '          }\n' +
      '        ]\n' +
      '      }\n' +
      '    ],\n' +
      '    "modbusWrites": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusWrite",\n' +
      '        "address": "5014",\n' +
      '        "factorToValue": null,\n' +
      '        "type": "Holding",\n' +
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
      '        "address": "5004",\n' +
      '        "factorToValue": 1000.0,\n' +
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
      '}');
    templates['Phoenix Contact EM-CP-PP-ETH'] = JSON.parse('{\n' +
      '  "@class": "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger",\n' +
      '  "voltage": null,\n' +
      '  "phases": null,\n' +
      '  "pollInterval": null,\n' +
      '  "startChargingStateDetectionDelay": 300,\n' +
      '  "forceInitialCharging": null,\n' +
      '  "vehicles": [],\n' +
      '  "modbusControl": {\n' +
      '    "@class": "de.avanux.smartapplianceenabler.modbus.EVModbusControl",\n' +
      '    "modbusReads": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusRead",\n' +
      '        "address": "100",\n' +
      '        "type": "Input",\n' +
      '        "valueType": "String",\n' +
      '        "byteOrder": null,\n' +
      '        "words": null,\n' +
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
      '        ]\n' +
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
      '    "modbusReads": [\n' +
      '      {\n' +
      '        "@class": "de.avanux.smartapplianceenabler.modbus.ModbusRead",\n' +
      '        "address": "100",\n' +
      '        "type": "Input",\n' +
      '        "valueType": "String",\n' +
      '        "byteOrder": null,\n' +
      '        "words": null,\n' +
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
      '        ]\n' +
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
    return templates;
  }
}
