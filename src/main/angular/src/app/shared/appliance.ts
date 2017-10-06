/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

export class Appliance {

  meterType: string;
  switchType: string;

  // Device2EM
  id: string;
  name: string;
  type: string;
  serial: string;
  vendor: string;
  maxPowerConsumption: string;
  currentPowerMethod: string;
  interruptionsAllowed: boolean;

  // =============
  // === Meter ===
  // =============

  // === S0ElectricityMeter ===
  meterGpio: string;
  meterPinPullResistance: string;
  meterImpulsesPerKwh: string;
  meterMeasurementInterval: string;
  meterPowerOnAlways: boolean;

  // === S0ElectricityMeterNetworked ===
  meterPulseReceiverID: string;
  // meterImpulsesPerKwh: string;
  // meterMeasurementInterval: string;
  // meterPowerOnAlways: boolean;

  // === ModbusElectricityMeter ===
  meterSlaveAddress: string;
  meterRegisterAddress: string;
  meterPollInterval: string;
  // meterMeasurementInterval: string;

  // === HttpElectricityMeter ===
  meterUrl: string;
  meterUsername: string;
  meterPassword: string;
  meterContentType: string;
  meterData: string;
  meterPowerValueExtractionRegex: string;
  meterFactorToWatt: string;
  // meterPollInterval: string;
  // meterMeasurementInterval: string;

  // ==============================================
  // === Switch DON'T REUSE METER VARIABLES !!! ===
  // ==============================================

  // === StartingCurrentSwitch ===
  switchStartingCurrentSwitch: boolean;
  switchPowerThreshold: string;
  switchStartingCurrentDetectionDuration: string;
  switchFinishedCurrentDetectionDuration: string;

  // === Switch ===
  switchGpio: string;
  switchReverseStates: boolean;

  // === ModbusSwitch ===
  switchSlaveAddress: string;
  switchRegisterAddress: string;

  // === HttpSwitch ===
  switchOnUrl: string;
  switchOffUrl: string;
  switchUsername: string;
  switchPassword: string;
  switchContentType: string;
  switchOnData: string;
  switchOffData: string;
}
