import {Selector} from 'testcafe';

export class SideMenu {

  private static applianceAnchor = Selector('a[href="/appliance"]');

  public static async clickNewAppliance(t: TestController): Promise<TestController> {
    await t.click(SideMenu.applianceAnchor);
    return t;
  }

  public static appliance(id: string): Selector {
    return Selector(`a[href="/appliance/${id}"]`);
  }
}
