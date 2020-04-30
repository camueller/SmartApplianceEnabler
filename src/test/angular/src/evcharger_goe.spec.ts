import {baseUrl} from './page/page';
import {
  configurationKey,
  createAndAssertAppliance,
  createAndAssertControl,
  createAndAssertElectricVehicle,
  createAndAssertMeter,
  fixtureName
} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {EvCharger} from '../../../main/angular/src/app/control/evcharger/ev-charger';
import {EvChargerTemplates} from '../../../main/angular/src/app/control/evcharger/ev-charger-templates';
import {evchargerGoe} from './fixture/appliance/evcharger-goe';
import {httpMeter_goeCharger} from './fixture/meter/goe-meter';
import {HttpElectricityMeter} from '../../../main/angular/src/app/meter/http/http-electricity-meter';
import {nissan_leaf} from './fixture/control/electricvehicle/nissan_leaf';
import {generateApplianceId} from './shared/appliance-id-generator';

fixture('Wallbox go-eCharger').page(baseUrl());

function createApplianceConfiguration(): ApplianceConfiguration {
  const configuration = new ApplianceConfiguration({
    appliance: {...evchargerGoe, id: generateApplianceId()},
    meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_goeCharger},
    control: {type: EvCharger.TYPE, evCharger: new EvCharger(EvChargerTemplates.getTemplates()['go-eCharger'])},
    controlTemplate: 'go-eCharger'
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
