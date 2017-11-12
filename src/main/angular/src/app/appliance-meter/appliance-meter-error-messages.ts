import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {ErrorMessages} from '../shared/error-messages';
import {TranslateService} from '@ngx-translate/core';

export class ApplianceMeterErrorMessages extends ErrorMessages {

  constructor(protected translate: TranslateService) {
    super('ApplianceMeterComponent.error.',
      [
        new ErrorMessage('s0ElectricityMeter_gpio', ValidatorType.required),
        new ErrorMessage('s0ElectricityMeter_gpio', ValidatorType.pattern),
        new ErrorMessage('s0ElectricityMeter_impulsesPerKwh', ValidatorType.required),
        new ErrorMessage('s0ElectricityMeter_impulsesPerKwh', ValidatorType.pattern),
        new ErrorMessage('s0ElectricityMeterNetworked_impulsesPerKwh', ValidatorType.required),
        new ErrorMessage('s0ElectricityMeterNetworked_impulsesPerKwh', ValidatorType.pattern),
        new ErrorMessage('modbusElectricityMeter_slaveAddress', ValidatorType.required),
        new ErrorMessage('modbusElectricityMeter_slaveAddress', ValidatorType.pattern),
        new ErrorMessage('modbusElectricityMeter_registerAddress', ValidatorType.required),
        new ErrorMessage('modbusElectricityMeter_pollInterval', ValidatorType.pattern),
        new ErrorMessage('modbusElectricityMeter_measurementInterval', ValidatorType.required),
        new ErrorMessage('modbusElectricityMeter_measurementInterval', ValidatorType.pattern),
        new ErrorMessage('httpElectricityMeter_url', ValidatorType.required),
        new ErrorMessage('httpElectricityMeter_url', ValidatorType.pattern),
        new ErrorMessage('httpElectricityMeter_factorToWatt', ValidatorType.pattern),
        new ErrorMessage('httpElectricityMeter_pollInterval', ValidatorType.pattern),
        new ErrorMessage('httpElectricityMeter_measurementInterval', ValidatorType.pattern)
      ], translate
    );
  }
}
