import {baseUrl} from './page/page';

fixture('Jump between heat pump and washing machine')
  .page(baseUrl())
  .before(async ctx => {
    // ctx.heatPump = TestContext.getHeatPump();
  });

test('Test it', async t => {
});
