import {Appliance} from './appliance';

export class ApplianceTestdata {

  public static getApplianceId(): string {
    return 'F-00000001-000000000001-00';
  }

  public static create(): Appliance {
    return new Appliance({
      id: this.getApplianceId(),
      name: 'WFO2842',
      type: 'WashingMachine',
      serial: '12345678',
      vendor: 'Bosch',
      maxPowerConsumption: '4000',
      currentPowerMethod: 'Measurement',
      interruptionsAllowed: true
    });
  }
}
