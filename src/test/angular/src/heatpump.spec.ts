import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {assertAppliance, assertControl, assertMeter, createAppliance, createControl, createMeter} from './shared/helper';
import {TopMenu} from './page/top-menu.page';

fixture('Heat pump')
  .page(baseUrl());

test('Create appliance', async t => {
  t.fixtureCtx.heatPump = TestContext.getHeatPump(t);
  await createAppliance(t, t.fixtureCtx.heatPump.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, t.fixtureCtx.heatPump.appliance);
});

test('Create meter', async t => {
  await createMeter(t, t.fixtureCtx.heatPump.appliance.id, t.fixtureCtx.heatPump.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, t.fixtureCtx.heatPump.appliance.id, t.fixtureCtx.heatPump.meter);
});

test('Create control', async t => {
  await createControl(t, t.fixtureCtx.heatPump.appliance.id, t.fixtureCtx.heatPump.control);
  await TopMenu.clickStatus(t);
  await assertControl(t, t.fixtureCtx.heatPump.appliance.id, t.fixtureCtx.heatPump.control);
});
