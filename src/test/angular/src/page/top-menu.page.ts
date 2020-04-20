import {Selector} from 'testcafe';
import {clickButton} from '../shared/form';

export class TopMenu {

  private static SIDEMENU = Selector('mat-toolbar > div > button');

  public static async clickMenu(t: TestController) {
    await clickButton(t, TopMenu.SIDEMENU);
  }
}
