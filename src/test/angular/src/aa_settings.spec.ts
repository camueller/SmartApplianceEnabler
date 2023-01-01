import {baseUrl} from './page/page';
import {settings} from './fixture/settings/settings';
import {SettingsPage} from './page/settings/settings.page';
import {testSpeed} from './shared/helper';

fixture('Settings')
    .beforeEach(async t => {
      await t.maximizeWindow();
      await t.setTestSpeed(testSpeed);
    })
    .page(baseUrl());

test('Modbus', async t => {
  await SettingsPage.createAndAssertSettings(t, settings);
});
