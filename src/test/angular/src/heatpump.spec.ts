import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {assertAppliance, assertControl, assertMeter, createAppliance, createControl, createMeter} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
import {ApplianceConfiguration} from './shared/appliance-configuration';

const fixtureName = 'Heat pump';
fixture(fixtureName).page(baseUrl());

test('Create appliance', async t => {
  const heatPump = TestContext.getHeatPump(t);
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  t.fixtureCtx[key] = heatPump;
  await createAppliance(t, heatPump.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, heatPump.appliance);
});

test('Create meter', async t => {
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  const heatPump: ApplianceConfiguration = t.fixtureCtx[key];
  await createMeter(t, heatPump.appliance.id, heatPump.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, heatPump.appliance.id, heatPump.meter);
});

test('Create control', async t => {
  const key = TestContext.runnerConfigurationKey(t, fixtureName);
  const heatPump: ApplianceConfiguration = t.fixtureCtx[key];
  await createControl(t, heatPump.appliance.id, heatPump.control);
  await TopMenu.clickStatus(t);
  await assertControl(t, heatPump.appliance.id, heatPump.control);
});
