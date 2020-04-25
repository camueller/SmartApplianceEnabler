import {EvCharger} from '../../../../../main/angular/src/app/control/evcharger/ev-charger';
import {
  assertCheckbox,
  assertInput,
  assertSelect,
  inputText,
  selectOptionByAttribute,
  selectorCheckboxByFormControlName,
  selectorCheckboxCheckedByFormControlName,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';
import {settings} from '../../fixture/settings/settings';
import {ControlPage} from './control.page';
import {EvchargerModbusPage} from './evcharger-modbus.page';
import {EvChargerProtocol} from '../../../../../main/angular/src/app/control/evcharger/ev-charger-protocol';

export class EvchargerPage extends ControlPage {

  public static async setEvChargerFromTemplate(t: TestController, templateName: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('template'), templateName);
    await EvchargerModbusPage.setIdRef(t, settings.modbusSettings[0].modbusTcpId);
  }

  public static async assertEvCharger(t: TestController, evCharger: EvCharger) {
    await EvchargerPage.assertProtocol(t, evCharger.protocol);
    await EvchargerPage.assertVoltage(t, evCharger.voltage && evCharger.voltage.toString());
    await EvchargerPage.assertPhases(t, evCharger.phases && evCharger.phases.toString());
    await EvchargerPage.assertPollInterval(t, evCharger.pollInterval && evCharger.pollInterval.toString());
    await EvchargerPage.assertStartChargingStateDetectionDelay(t,
      evCharger.startChargingStateDetectionDelay && evCharger.startChargingStateDetectionDelay.toString());
    await EvchargerPage.assertForceInitialCharging(t, !!evCharger.forceInitialCharging);
    if (evCharger.protocol === EvChargerProtocol.MODBUS) {
      await EvchargerModbusPage.assertEvChargerModbus(t, evCharger.modbusControl);
    }
  }

  public static async setProtocol(t: TestController, protocol: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('protocol'), protocol);
  }
  public static async assertProtocol(t: TestController, protocol: string) {
    await assertSelect(t, selectorSelectedByFormControlName('protocol'), protocol, 'ControlEvchargerComponent.protocol.');
  }

  public static async setVoltage(t: TestController, voltage: string) {
    await inputText(t, selectorInputByFormControlName('voltage'), voltage);
  }
  public static async assertVoltage(t: TestController, voltage: string) {
    await assertInput(t, selectorInputByFormControlName('voltage'), voltage);
  }

  public static async setPhases(t: TestController, phases: string) {
    await inputText(t, selectorInputByFormControlName('phases'), phases);
  }
  public static async assertPhases(t: TestController, phases: string) {
    await assertInput(t, selectorInputByFormControlName('phases'), phases);
  }

  public static async setPollInterval(t: TestController, pollInterval: string) {
    await inputText(t, selectorInputByFormControlName('pollInterval'), pollInterval);
  }
  public static async assertPollInterval(t: TestController, pollInterval: string) {
    await assertInput(t, selectorInputByFormControlName('pollInterval'), pollInterval);
  }

  public static async setStartChargingStateDetectionDelay(t: TestController, startChargingStateDetectionDelay: string) {
    await inputText(t, selectorInputByFormControlName('startChargingStateDetectionDelay'), startChargingStateDetectionDelay);
  }
  public static async assertStartChargingStateDetectionDelay(t: TestController, startChargingStateDetectionDelay: string) {
    await assertInput(t, selectorInputByFormControlName('startChargingStateDetectionDelay'), startChargingStateDetectionDelay);
  }

  public static async setForceInitialCharging(t: TestController, forceInitialCharging: boolean) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('forceInitialCharging'), forceInitialCharging);
  }
  public static async assertForceInitialCharging(t: TestController, forceInitialCharging: boolean) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('forceInitialCharging'), forceInitialCharging);
  }
}
