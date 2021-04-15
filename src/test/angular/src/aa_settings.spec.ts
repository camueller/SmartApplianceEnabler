import {baseUrl} from './page/page';
import {settings} from './fixture/settings/settings';
import {SettingsPage} from './page/settings/settings.page';

fixture('Settings').page(baseUrl());

test('Modbus', async t => {
  await SettingsPage.createAndAssertSettings(t, settings);
});
