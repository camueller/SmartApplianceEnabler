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
  public static async clickAppliance(t: TestController, id: string): Promise<TestController> {
    await t.click(SideMenu.appliance(id));
    return t;
  }

  public static meter(id: string): Selector {
    return Selector(`a[href="/meter/${id}"]`);
  }
  public static async clickMeter(t: TestController, id: string): Promise<TestController> {
    await t.click(SideMenu.meter(id));
    return t;
  }

  public static control(id: string): Selector {
    return Selector(`a[href="/control/${id}"]`);
  }
  public static async clickControl(t: TestController, id: string): Promise<TestController> {
    await t.click(SideMenu.control(id));
    return t;
  }
}
