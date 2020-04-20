import {Selector} from 'testcafe';
import {saeRestartTimeout} from '../shared/timeout';
import {TopMenu} from './top-menu.page';
import {clickButton} from '../shared/form';

export class SideMenu {

  private static SETTINGS = Selector('app-sidenav a[href="/settings"]');
  private static STATUS = Selector('app-sidenav a[href="/status"]');

  private static async openSideMenuIfClosed(t: TestController) {
    const sideMenuOpen = await SideMenu.newAppliance().visible;
    if (! sideMenuOpen) {
      await TopMenu.clickMenu(t);
    }
  }

  public static async clickSettings(t: TestController) {
    await SideMenu.openSideMenuIfClosed(t);
    await clickButton(t, SideMenu.SETTINGS);
  }

  public static async clickStatus(t: TestController) {
    await SideMenu.openSideMenuIfClosed(t);
    await clickButton(t, SideMenu.STATUS);
  }

  public static newAppliance(): Selector {
    return Selector('a[href="/appliance"]');
  }

  public static async clickNewAppliance(t: TestController) {
    await SideMenu.openSideMenuIfClosed(t);
    await clickButton(t, SideMenu.newAppliance());
  }

  public static appliance(id: string): Selector {
    return Selector(`a[href="/appliance/${id}"]`, {timeout: saeRestartTimeout});
  }

  public static async clickAppliance(t: TestController, id: string) {
    await SideMenu.openSideMenuIfClosed(t);
    await clickButton(t, SideMenu.appliance(id));
    await Selector('app-appliance', {timeout: saeRestartTimeout}).exists;
  }

  public static meter(id: string): Selector {
    return Selector(`a[href="/meter/${id}"]`, {timeout: saeRestartTimeout});
  }

  public static async clickMeter(t: TestController, id: string) {
    await SideMenu.openSideMenuIfClosed(t);
    await clickButton(t, SideMenu.meter(id));
    await Selector('app-meter', {timeout: saeRestartTimeout}).exists;
  }

  public static control(id: string): Selector {
    return Selector(`a[href="/control/${id}"]`, {timeout: saeRestartTimeout});
  }

  public static async clickControl(t: TestController, id: string) {
    await SideMenu.openSideMenuIfClosed(t);
    await clickButton(t, SideMenu.control(id));
    await Selector('app-control', {timeout: saeRestartTimeout}).exists;
  }
}
