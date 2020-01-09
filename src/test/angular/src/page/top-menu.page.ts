import {Selector} from 'testcafe';

export class TopMenu {

  private static APPLIANCES = Selector('a.launch'); // FIXME provide better selector
  private static STATUS = Selector('a[href="/status"]');

  public static async clickAppliances(t: TestController): Promise<TestController> {
    await t.click(TopMenu.APPLIANCES);
    return t;
  }

  public static async clickStatus(t: TestController): Promise<TestController> {
    await t.click(TopMenu.STATUS);
    return t;
  }
}
