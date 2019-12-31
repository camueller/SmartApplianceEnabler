import {TopMenu} from './page/top-menu.page';
import {SideMenu} from './page/side.menu.page';

fixture('Appliance')
    .page('http://ec2-18-221-3-23.us-east-2.compute.amazonaws.com');


test('Create appliance', async t => {
    await TopMenu.clickAppliances(t);
    await SideMenu.clickNewAppliance(t);
    await t.wait(10000);
});
