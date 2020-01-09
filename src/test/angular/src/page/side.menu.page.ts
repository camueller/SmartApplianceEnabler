import {Selector} from 'testcafe';
import {saeRestartTimeout} from '../shared/timeout';
import {TopMenu} from './top-menu.page';

export class SideMenu {

  private static async openSideMenuIfClosed(t: TestController) {
    const sideMenuOpen = await SideMenu.newAppliance().visible;
    if (! sideMenuOpen) {
      await TopMenu.clickAppliances(t);
    }
  }

  public static newAppliance(): Selector {
    return Selector('a[href="/appliance"]');
  }

  public static async clickNewAppliance(t: TestController): Promise<TestController> {
    await SideMenu.openSideMenuIfClosed(t);
    await t.click(SideMenu.newAppliance());
    return t;
  }

  public static appliance(id: string): Selector {
    return Selector(`a[href="/appliance/${id}"]`);
  }

  public static async clickAppliance(t: TestController, id: string): Promise<TestController> {
    await SideMenu.openSideMenuIfClosed(t);
    await t.click(SideMenu.appliance(id));
    return t;
  }

  public static meter(id: string): Selector {
    return Selector(`a[href="/meter/${id}"]`, {timeout: saeRestartTimeout});
  }

  public static async clickMeter(t: TestController, id: string): Promise<TestController> {
    await SideMenu.openSideMenuIfClosed(t);
    await t.click(SideMenu.meter(id));
    return t;
  }

  public static control(id: string): Selector {
    return Selector(`a[href="/control/${id}"]`, {timeout: saeRestartTimeout});
  }

  public static async clickControl(t: TestController, id: string): Promise<TestController> {
    await SideMenu.openSideMenuIfClosed(t);
    await t.click(SideMenu.control(id));
    return t;
  }
}
