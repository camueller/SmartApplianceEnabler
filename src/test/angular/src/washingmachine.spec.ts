import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {createAppliance, createMeter} from './shared/helper';

fixture('Washing Machine')
  .page(baseUrl());

test('Create appliance', async t => {
  t.fixtureCtx.washingMachine = TestContext.getWashingMachine(t);
  await createAppliance(t, t.fixtureCtx.washingMachine.appliance);
});

test('Create meter', async t => {
  await createMeter(t, t.fixtureCtx.washingMachine.appliance.id, t.fixtureCtx.washingMachine.meter);
});
