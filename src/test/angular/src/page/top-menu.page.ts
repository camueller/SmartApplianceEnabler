import {clickButton} from '../shared/form';

export class TopMenu {

  private static SIDEMENU_SELECTOR = 'button.HeaderComponent__menuicon';

  public static async clickMenu(t: TestController) {
    await clickButton(t, TopMenu.SIDEMENU_SELECTOR);
  }
}
