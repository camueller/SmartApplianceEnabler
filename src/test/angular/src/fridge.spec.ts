import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {createAppliance, createControl, createMeter} from './shared/helper';

fixture('Fridge')
  .page(baseUrl());

test('Create appliance', async t => {
  t.fixtureCtx.fridge = TestContext.getFridge(t);
  await createAppliance(t, t.fixtureCtx.fridge.appliance);
});

test('Create meter', async t => {
  await createMeter(t, t.fixtureCtx.fridge.appliance.id, t.fixtureCtx.fridge.meter);
});

test('Create control', async t => {
  await createControl(t, t.fixtureCtx.fridge.appliance.id, t.fixtureCtx.fridge.control);
});
