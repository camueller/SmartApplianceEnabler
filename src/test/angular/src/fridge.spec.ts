import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {assertAppliance, assertControl, assertMeter, createAppliance, createControl, createMeter} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
import {ApplianceConfiguration} from './shared/appliance-configuration';

const fixtureName = 'Fridge';
fixture(fixtureName).page(baseUrl());

test('Create appliance', async t => {
  const fridge = TestContext.getFridge(t);
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  t.fixtureCtx[key] = fridge;
  await createAppliance(t, fridge.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, fridge.appliance);
});

test('Create meter', async t => {
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  const fridge: ApplianceConfiguration = t.fixtureCtx[key];
  await createMeter(t, fridge.appliance.id, fridge.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, fridge.appliance.id, fridge.meter);
});

test('Create control', async t => {
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  const fridge: ApplianceConfiguration = t.fixtureCtx[key];
  await createControl(t, fridge.appliance.id, fridge.control);
  await TopMenu.clickStatus(t);
  await assertControl(t, fridge.appliance.id, fridge.control);
});
