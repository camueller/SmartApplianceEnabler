import {Selector} from 'testcafe';

export class TopMenu {

  // FIXME provide better selector
  private static APPLIANCES = Selector('a.launch');

  public static async clickAppliances(t: TestController): Promise<TestController> {
    await t.click(TopMenu.APPLIANCES);
    return t;
  }
}
