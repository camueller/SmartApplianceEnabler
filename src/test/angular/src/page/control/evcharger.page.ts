import {EvCharger} from '../../../../../main/angular/src/app/control/evcharger/ev-charger';
import {
  assertCheckbox,
  assertInput,
  assertSelectOption,
  clickButton,
  inputText,
  selectOption,
  selectorButton,
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
import {EvchargerHttpPage} from './evcharger-http.page';
import {ElectricVehicle} from '../../../../../main/angular/src/app/control/evcharger/electric-vehicle/electric-vehicle';
import {SideMenu} from '../side.menu.page';

export class EvchargerPage extends ControlPage {

  private static electricVehicleSelectorBase(httpReadIndex: number) {
    return `*[formarrayname="electricVehicles"] div:nth-child(${httpReadIndex + 1}) > app-electric-vehicle`;
  }

  public static async setEvChargerFromTemplate(t: TestController, evCharger: EvCharger, templateName: string) {
    await selectOption(t, selectorSelectByFormControlName('template'), templateName);
    if (evCharger.protocol === EvChargerProtocol.MODBUS) {
      await EvchargerModbusPage.setIdRef(t, settings.modbusSettings[0].modbusTcpId);
    }
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
    if (evCharger.protocol === EvChargerProtocol.HTTP) {
      await EvchargerHttpPage.assertEvChargerHttp(t, evCharger.httpControl);
    }
  }

  public static async setElectricVehicles(t: TestController, applianceId: string, electricVehicles: ElectricVehicle[]) {
    for (let i = 0; i < electricVehicles.length; i++) {
      await this.setElectricVehicle(t, applianceId, electricVehicles[i], i, true, i > 0, false);
    }
  }
  public static async assertElectricVehicles(t: TestController, applianceId: string, electricVehicles: ElectricVehicle[]) {
    for (let i = 0; i < electricVehicles.length; i++) {
      await this.assertElectricVehicle(t, applianceId, electricVehicles[i], i, true);
    }
  }

  public static async setElectricVehicle(t: TestController, applianceId: string, ev: ElectricVehicle, index: number,
                                         clickControl: boolean, clickAdd: boolean, clickSave: boolean) {
    if (clickControl) {
      await SideMenu.clickControl(t, applianceId);
    }
    if (clickAdd) {
      await clickButton(t, selectorButton(undefined, 'ControlEvchargerComponent__addElectricVehicle'));
    }
    await EvchargerPage.setName(t, ev.name, index);
    await EvchargerPage.setBatteryCapacity(t, ev.batteryCapacity && ev.batteryCapacity.toString(), index);
    await EvchargerPage.setPhasesEv(t, ev.phases && ev.phases.toString(), index);
    await EvchargerPage.setMaxChargePower(t, ev.maxChargePower && ev.maxChargePower.toString(), index);
    await EvchargerPage.setChargeLoss(t, ev.chargeLoss && ev.chargeLoss.toString(), index);
    await EvchargerPage.setDefaultSocManual(t, ev.defaultSocManual && ev.defaultSocManual.toString(), index);
    await EvchargerPage.setDefaultSocOptionalEnergy(t, ev.defaultSocOptionalEnergy && ev.defaultSocOptionalEnergy.toString(), index);
    if (ev.socScript) {
      await EvchargerPage.setScriptFilename(t, ev.socScript.script, index);
      await EvchargerPage.setScriptExtractionRegex(t, ev.socScript.extractionRegex, index);
    }
    if (clickSave) {
      await this.clickSave(t);
    }
  }
  public static async assertElectricVehicle(t: TestController, applianceId: string, ev: ElectricVehicle, index: number,
                                            clickControl: boolean) {
    if (clickControl) {
      await SideMenu.clickControl(t, applianceId);
    }
    await EvchargerPage.assertName(t, ev.name, index);
    await EvchargerPage.assertBatteryCapacity(t, ev.batteryCapacity && ev.batteryCapacity.toString(), index);
    await EvchargerPage.assertPhasesEv(t, ev.phases && ev.phases.toString(), index);
    await EvchargerPage.assertMaxChargePower(t, ev.maxChargePower && ev.maxChargePower.toString(), index);
    await EvchargerPage.assertChargeLoss(t, ev.chargeLoss && ev.chargeLoss.toString(), index);
    await EvchargerPage.assertDefaultSocManual(t, ev.defaultSocManual && ev.defaultSocManual.toString(), index);
    await EvchargerPage.assertDefaultSocOptionalEnergy(t, ev.defaultSocOptionalEnergy && ev.defaultSocOptionalEnergy.toString(), index);
    if (ev.socScript) {
      await EvchargerPage.assertScriptFilename(t, ev.socScript.script, index);
      await EvchargerPage.assertScriptExtractionRegex(t, ev.socScript.extractionRegex, index);
    }
  }

  public static async setProtocol(t: TestController, protocol: string) {
    await selectOption(t, selectorSelectByFormControlName('protocol'), protocol);
  }
  public static async assertProtocol(t: TestController, protocol: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('protocol'), protocol, 'ControlEvchargerComponent.protocol.');
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

  /**
   * Electric vehicle
   */

  public static async setName(t: TestController, name: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('name', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), name);
  }
  public static async assertName(t: TestController, name: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('name', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), name);
  }

  public static async setBatteryCapacity(t: TestController, batteryCapacity: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('batteryCapacity', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), batteryCapacity);
  }
  public static async assertBatteryCapacity(t: TestController, batteryCapacity: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('batteryCapacity', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), batteryCapacity);
  }

  public static async setPhasesEv(t: TestController, phases: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('phases', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), phases);
  }
  public static async assertPhasesEv(t: TestController, phases: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('phases', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), phases);
  }

  public static async setMaxChargePower(t: TestController, maxChargePower: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('maxChargePower', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), maxChargePower);
  }
  public static async assertMaxChargePower(t: TestController, maxChargePower: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('maxChargePower', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), maxChargePower);
  }

  public static async setChargeLoss(t: TestController, chargeLoss: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('chargeLoss', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), chargeLoss);
  }
  public static async assertChargeLoss(t: TestController, chargeLoss: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('chargeLoss', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), chargeLoss);
  }

  public static async setDefaultSocManual(t: TestController, defaultSocManual: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('defaultSocManual', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), defaultSocManual);
  }
  public static async assertDefaultSocManual(t: TestController, defaultSocManual: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('defaultSocManual', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), defaultSocManual);
  }

  public static async setDefaultSocOptionalEnergy(t: TestController, defaultSocOptionalEnergy: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('defaultSocOptionalEnergy', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), defaultSocOptionalEnergy);
  }
  public static async assertDefaultSocOptionalEnergy(t: TestController, defaultSocOptionalEnergy: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('defaultSocOptionalEnergy', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), defaultSocOptionalEnergy);
  }

  public static async setScriptFilename(t: TestController, scriptFilename: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('scriptFilename', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), scriptFilename);
  }
  public static async assertScriptFilename(t: TestController, scriptFilename: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('scriptFilename', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), scriptFilename);
  }

  public static async setScriptExtractionRegex(t: TestController, scriptExtractionRegex: string, evIndex: number) {
    await inputText(t, selectorInputByFormControlName('scriptExtractionRegex', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), scriptExtractionRegex);
  }
  public static async assertScriptExtractionRegex(t: TestController, scriptExtractionRegex: string, evIndex: number) {
    await assertInput(t, selectorInputByFormControlName('scriptExtractionRegex', undefined,
      EvchargerPage.electricVehicleSelectorBase(evIndex)), scriptExtractionRegex);
  }
}
