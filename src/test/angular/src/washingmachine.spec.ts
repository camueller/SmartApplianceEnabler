import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {assertAppliance, assertMeter, createAppliance, createMeter} from './shared/helper';
import {TopMenu} from './page/top-menu.page';

fixture('Washing Machine')
  .page(baseUrl());

test('Create appliance', async t => {
  t.fixtureCtx.washingMachine = TestContext.getWashingMachine(t);
  await createAppliance(t, t.fixtureCtx.washingMachine.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, t.fixtureCtx.washingMachine.appliance);
});

test('Create meter', async t => {
  await createMeter(t, t.fixtureCtx.washingMachine.appliance.id, t.fixtureCtx.washingMachine.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, t.fixtureCtx.washingMachine.appliance.id, t.fixtureCtx.washingMachine.meter);
});
