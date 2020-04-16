import {Selector} from 'testcafe';

export class TopMenu {

  private static SIDEMENU = Selector('mat-toolbar > div > button');

  public static async clickMenu(t: TestController) {
    await t.click(TopMenu.SIDEMENU);
  }
}
