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

import { Injectable } from '@angular/core';
import {Appliance} from './appliance';
import {Schedule} from './schedule';
import {DayTimeframe} from './day-timeframe';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';
import {Settings} from './settings';

@Injectable()
export class ApplianceService {

  appliances: Appliance[];
  applianceSchedules: Map<string, Schedule[]> = new Map();
  settings: Settings;

  constructor() {
    const a1: Appliance = new Appliance();
    a1.id = 'F-00000001-000000000001-00';
    a1.name = 'SMI53M72EU';
    a1.type = 'DishWasher';
    a1.serial = '016120242942002153';
    a1.vendor = 'Bosch';
    a1.maxPowerConsumption = '1500';
    a1.currentPowerMethod = 'Measurement';
    a1.interruptionsAllowed = true;

    a1.meterType = 'S0ElectricityMeter';
    a1.meterGpio = '2';
    a1.meterPinPullResistance = 'PULL_DOWN';
    a1.meterImpulsesPerKwh = '1000';
    a1.meterMeasurementInterval = '300';
    a1.meterPowerOnAlways = true;

    a1.switchType = 'Switch';
    a1.switchGpio = '3';
    a1.switchStartingCurrentSwitch = true;
    a1.switchPowerThreshold = '30';
    a1.switchStartingCurrentDetectionDuration = '180';
    a1.switchFinishedCurrentDetectionDuration = '300';

    const a2: Appliance = new Appliance();
    a2.id = 'F-00000001-000000000002-00';
    a2.name = 'WFO2842';
    a2.type = 'WashingMachine';
    a2.serial = '15FD8512701147';
    a2.vendor = 'Siemens';
    a2.maxPowerConsumption = '2000';
    a2.currentPowerMethod = 'Estimation';
    a2.interruptionsAllowed = false;

    a2.meterType = 'HttpElectricityMeter';
    a2.meterUrl = 'http://192.168.69.74:10000/smartplug.cgi';
    a2.meterUsername = 'admin';
    a2.meterPassword = '12345678';
    a2.meterContentType = 'application/xml';
    a2.meterData = '&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;get&quot;&gt;&lt;NOW_POWER&gt;&lt;Device.System.Power.NowCurrent&gt;&lt;/Device.System.Power.NowCurrent&gt;&lt;Device.System.Power.NowPower&gt;&lt;/Device.System.Power.NowPower&gt;&lt;/NOW_POWER&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;';
    a2.meterPowerValueExtractionRegex = '.*NowPower>(\\d*.{0,1}\\d+).*';
    a2.meterFactorToWatt = '0.01';
    a2.meterPollInterval = '15';
    a2.meterMeasurementInterval = '300';

    a2.switchType = 'HttpSwitch';
    a2.switchOnUrl = 'http://192.168.69.74:10000/smartplug.cgi';
    a2.switchOffUrl = 'http://192.168.69.74:10000/smartplug.cgi';
    a2.switchUsername = 'admin';
    a2.switchPassword = '12345678';
    a2.switchContentType = 'application/xml';
    a2.switchOnData = '&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;ON&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;';
    a2.switchOffData = '&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;OFF&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;';

    const a3: Appliance = new Appliance();
    a3.id = 'F-00000001-000000000003-00';
    a3.name = 'Bettar 14';
    a3.type = 'Pump';
    a3.serial = '0123456789';
    a3.vendor = 'Speck';
    a3.maxPowerConsumption = '1000';
    a3.currentPowerMethod = 'Measurement';
    a3.interruptionsAllowed = true;

    a3.meterType = 'ModbusElectricityMeter';
    a3.meterSlaveAddress = '1';
    a3.meterRegisterAddress = '0C';
    a3.meterPollInterval = '10';
    a3.meterMeasurementInterval = '60';

    a3.switchType = 'ModbusSwitch';
    a3.switchSlaveAddress = '2';
    a3.switchRegisterAddress = '0A';

    this.appliances = [a1, a2, a3];

    const a1s1: Schedule = new Schedule();
    a1s1.timeframeType = 'DayTimeframe';
    a1s1.minRunningTime = '10800';
    a1s1.dayTimeframe = new DayTimeframe();
    a1s1.dayTimeframe.start = '6:00:00';
    a1s1.dayTimeframe.end = '16:00:00';
    a1s1.dayTimeframe.days = ['1', '2', '3', '4', '5'];

    const a1s2: Schedule = new Schedule();
    a1s2.timeframeType = 'DayTimeframe';
    a1s2.minRunningTime = '10801';
    a1s2.dayTimeframe = new DayTimeframe();
    a1s2.dayTimeframe.start = '6:00:00';
    a1s2.dayTimeframe.end = '14:00:00';
    a1s2.dayTimeframe.days = ['6', '7', '8'];

    const a1s3: Schedule = new Schedule();
    a1s3.timeframeType = 'DayTimeframe';
    a1s3.minRunningTime = '10802';
    a1s3.dayTimeframe = new DayTimeframe();
    a1s3.dayTimeframe.start = '14:00:00';
    a1s3.dayTimeframe.end = '20:00:00';
    a1s3.dayTimeframe.days = ['6', '7', '8'];

    const a1Schedules: Schedule[] = [a1s1, a1s2, a1s3];
    this.applianceSchedules.set(a1.id, a1Schedules);

    const a2s1: Schedule = new Schedule();
    a2s1.minRunningTime = '43200';
    a2s1.timeframeType = 'ConsecutiveDaysTimeframe';
    a2s1.consecutiveDaysTimeframe = new ConsecutiveDaysTimeframe();
    a2s1.consecutiveDaysTimeframe.startDayOfWeek = '5';
    a2s1.consecutiveDaysTimeframe.startTime = '16:00:00';
    a2s1.consecutiveDaysTimeframe.endDayOfWeek = '7';
    a2s1.consecutiveDaysTimeframe.endTime = '20:00:00';

    const a2Schedules: Schedule[] = [a2s1];
    this.applianceSchedules.set(a2.id, a2Schedules);

    this.settings = new Settings();
    this.settings.holidayUrl = 'http://feiertage.jarmedia.de/api/?jahr={0}&#038;nur_land=HE';
    this.settings.pulseReceiverPort = '1234';
    this.settings.modbusTcpHost = '127.0.0.1';
    this.settings.modbusTcpPort = '4567';
  }

  getAppliances() {
    return this.appliances;
  }

  getAppliance(id: string): Appliance {
    let appliance: Appliance = new Appliance();
    this.appliances.forEach((item) => {
      if (item.id === id) {
        appliance = item;
      }
    });
    return appliance;
  }

  createAppliance(appliance: Appliance) {
    console.log('Appliance created');
    this.appliances.push(appliance);
  }

  updateAppliance(appliance: Appliance) {
    this.appliances.forEach((item, i) => {
      if (item.id === appliance.id) {
        item[i] = appliance;
        console.log('Appliance[' + i + '] updated');
      }
    });
  }

  getSchedules(id: string): Schedule[] {
    return this.applianceSchedules.get(id);
  }

  updateSchedules(id: string, schedules: Schedule[]) {
    console.log('Updated ' + schedules.length + ' schedules of appliance ' + id);
    this.applianceSchedules.set(id, schedules);
  }

  getSettings(): Settings {
    return this.settings;
  }

  updateSettings(settings: Settings) {
    this.settings = settings;
  }
}
