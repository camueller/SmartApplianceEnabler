import {baseUrl} from './page/page';
import {
  configurationKey,
  createAndAssertAppliance,
  createAndAssertControl,
  createAndAssertMeter,
  createAndAssertSchedules,
  fixtureName
} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {HttpElectricityMeter} from '../../../main/angular/src/app/meter/http/http-electricity-meter';
import {washingMachine as washingMachineAppliance} from './fixture/appliance/washingmachine';
import {httpMeter_complete, httpMeter_pollInterval} from './fixture/meter/http-meter';
import {generateApplianceId} from './shared/appliance-id-generator';
import {httpSwitch_2httpWrite_httpRead_complete} from './fixture/control/http-control';
import {HttpSwitch} from '../../../main/angular/src/app/control/http/http-switch';
import {startingCurrentSwitch} from './fixture/control/starting-current-switch';
import {runtimeRequest_dayTimeframe_weekday_weekend} from './fixture/schedule/runtimeRequest_dayTimeframe_weekday_weekend';

fixture('Washing Machine').page(baseUrl());

function createWashingMachine(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...washingMachineAppliance, id: generateApplianceId()},
    meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_pollInterval},
    control: {type: HttpSwitch.TYPE, httpSwitch: httpSwitch_2httpWrite_httpRead_complete,
      startingCurrentSwitchUsed: true, startingCurrentSwitch },
    schedules: runtimeRequest_dayTimeframe_weekday_weekend,
  });
}

test('Create appliance', async t => {
  await createAndAssertAppliance(t, createWashingMachine());
});

test('Create HTTP meter with specifc poll interval', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create HTTP switch with starting current detection', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create Schedules for weekdays and weekend', async t => {
  await createAndAssertSchedules(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
