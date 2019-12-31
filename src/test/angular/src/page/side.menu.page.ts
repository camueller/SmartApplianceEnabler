import {Selector} from 'testcafe';

export class SideMenu {

    private static NEW_APPLIANCE = 'a[href="/appliance"]';

    public static async clickNewAppliance(t: TestController): Promise<TestController> {
        await t.click(Selector(SideMenu.NEW_APPLIANCE));
        return t;
    }
}
