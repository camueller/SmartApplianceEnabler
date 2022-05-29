import {baseUrl} from './page/page';
import {
  configurationKey,
  createAndAssertAppliance,
  createAndAssertControl,
  createAndAssertElectricVehicle,
  createAndAssertMeter,
  createAndAssertSchedules,
  fixtureName
} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {EvCharger} from '../../../main/angular/src/app/control/evcharger/ev-charger';
import {evchargerGoe} from './fixture/appliance/evcharger-goe';
import {httpMeter_goeCharger} from './fixture/meter/goe-meter';
import {HttpElectricityMeter} from '../../../main/angular/src/app/meter/http/http-electricity-meter';
import {nissan_leaf} from './fixture/control/electricvehicle/nissan_leaf';
import {generateApplianceId} from './shared/appliance-id-generator';
import {tesla_model3} from './fixture/control/electricvehicle/tesla_model3';
import {socRequest_consecutiveDaysTimeframe} from './fixture/schedule/socRequest_consecutiveDaysTimeframe';
import evChargerTemplates from '../../../../run/evcharger-templates.json';

fixture('Wallbox go-eCharger').page(baseUrl());

function createApplianceConfiguration(): ApplianceConfiguration {
  const goECharger = evChargerTemplates.find(template => template.name === 'go-eCharger').template as unknown as EvCharger;
  const configuration = new ApplianceConfiguration({
    appliance: {...evchargerGoe, id: generateApplianceId()},
    meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_goeCharger},
    control: {type: EvCharger.TYPE, evCharger: goECharger},
    controlTemplate: 'go-eCharger',
    schedules: socRequest_consecutiveDaysTimeframe,
  });
  configuration.control.evCharger.vehicles = [
    nissan_leaf,
  ];
  return configuration;
}

test('Create appliance with interruptions allowed without timing specification', async t => {
  await createAndAssertAppliance(t, createApplianceConfiguration());
});

test('Create HTTP meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create HTTP control', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Add electric vehicle', async t => {
  await createAndAssertElectricVehicle(t, t.fixtureCtx[configurationKey(t, fixtureName(t))].appliance.id, tesla_model3, 1,
    true, true, true);
});

test('Create Schedule with SOC request from friday to sunday', async t => {
  const configuration: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createAndAssertSchedules(t, configuration, configuration.control.evCharger.vehicles[0].name);
});
