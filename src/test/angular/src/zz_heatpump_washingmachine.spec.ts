import {baseUrl} from './page/page';
import {GlobalContext} from './shared/global-context';
import {configurationKey} from './shared/helper';

fixture('Jump between heat pump and washing machine').page(baseUrl());

test('Test it', async t => {
  const washingMachine = GlobalContext.ctx[configurationKey(t, 'Washing Machine')];
});
