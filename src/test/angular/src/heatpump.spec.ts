import {baseUrl} from './page/page';
import {TestContext} from './shared/test-context';
import {createAppliance, createControl, createMeter} from './shared/helper';

fixture('Heat pump')
  .page(baseUrl())
  .before(async ctx => {
    ctx.heatPump = TestContext.getHeatPump();
  });

test('Create appliance', async t => {
  await createAppliance(t, t.fixtureCtx.heatPump.appliance);
});

test('Create meter', async t => {
  await createMeter(t, t.fixtureCtx.heatPump.appliance.id, t.fixtureCtx.heatPump.meter);
});

test('Create control', async t => {
  await createControl(t, t.fixtureCtx.heatPump.appliance.id, t.fixtureCtx.heatPump.control);
});
