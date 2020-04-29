import {clickButton} from '../shared/form';

export class TopMenu {

  private static SIDEMENU_SELECTOR = 'mat-toolbar > div > button';

  public static async clickMenu(t: TestController) {
    await clickButton(t, TopMenu.SIDEMENU_SELECTOR);
  }
}
