import {EvCharger} from './ev-charger';

export class EvChargerTemplates {

  static getTemplates(): { [name: string]: EvCharger } {
    const templates: { [name: string]: EvCharger } = {};
    templates['PhoenixContact'] = JSON.parse('{"@class":"de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger","voltage":230,"phases":1,"pollInterval":10,"startChargingStateDetectionDelay":300,"forceInitialCharging":false,"control":{"@class":"de.avanux.smartapplianceenabler.control.ev.EVModbusControl","slaveAddress":180,"configuration":[{"name":"VehicleNotConnected","address":"100","type":"InputString","extractionRegex":"(A)","write":false},{"name":"VehicleConnected","address":"100","type":"InputString","extractionRegex":"(B)","write":false},{"name":"Charging","address":"100","type":"InputString","extractionRegex":"(C|D)","write":false},{"name":"ChargingCompleted","address":"100","type":"InputString","extractionRegex":"(B)","write":false},{"name":"ChargingCompleted","address":"204","type":"Discrete","extractionRegex":null,"write":false},{"name":"StartCharging","address":"400","type":"Coil","value":"1","write":true},{"name":"StopCharging","address":"400","type":"Coil","value":"0","write":true},{"name":"ChargingCurrent","address":"300","type":"Holding","value":"0","write":true}]}}');
    return templates;
  }
}
