import {Selector} from 'testcafe';

export class TopMenu {

  private static APPLIANCES = Selector('a.launch'); // FIXME provide better selector
  private static STATUS = Selector('a[href="/status"]');
  private static SETTINGS = Selector('a[href="/settings"]');

  public static async clickAppliances(t: TestController) {
    await t.click(TopMenu.APPLIANCES);
  }

  public static async clickStatus(t: TestController) {
    await t.click(TopMenu.STATUS);
  }

  public static async clickSettings(t: TestController) {
    await t.click(TopMenu.SETTINGS);
  }
}
