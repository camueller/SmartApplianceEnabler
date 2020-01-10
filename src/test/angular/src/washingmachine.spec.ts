import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {assertAppliance, assertMeter, createAppliance, createMeter} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
import {ApplianceConfiguration} from './shared/appliance-configuration';

const fixtureName = 'Washing Machine';
fixture(fixtureName).page(baseUrl());

test('Create appliance', async t => {
  const washingMachine = TestContext.getWashingMachine(t);
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  t.fixtureCtx[key] = washingMachine;
  await createAppliance(t, washingMachine.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, washingMachine.appliance);
});

test('Create meter', async t => {
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  const washingMachine: ApplianceConfiguration = t.fixtureCtx[key];
  await createMeter(t, washingMachine.appliance.id, washingMachine.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, washingMachine.appliance.id, washingMachine.meter);
});
