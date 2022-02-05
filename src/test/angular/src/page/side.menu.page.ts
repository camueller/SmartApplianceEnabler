import {Selector} from 'testcafe';
import {TopMenu} from './top-menu.page';
import {clickButton} from '../shared/form';
import {StatusPage} from './status.page';
import {AppliancePage} from './appliance/appliance.page';
import {MeterPage} from './meter/meter.page';
import {ControlPage} from './control/control.page';
import {SchedulesPage} from './schedule/schedules.page';
import {isDebug} from '../shared/helper';

export class SideMenu {

  private static SETTINGS_SELECTOR = 'app-sidenav a[href="/settings"]';
  private static STATUS_SELECTOR = 'app-sidenav a[href="/status"]';

  private static async openSideMenuIfClosed(t: TestController) {
    const sideMenuOpen = await Selector('mat-sidenav.mat-drawer-opened').exists;
    if (! sideMenuOpen) {
      await TopMenu.clickMenu(t);
      await Selector('mat-sidenav.mat-drawer-opened').exists;
    }
  }

  private static async clickEntry(t: TestController, clickSelectorString: string, successFunction: () => Promise<boolean>) {
    await SideMenu.openSideMenuIfClosed(t);
    await t.expect(Selector(clickSelectorString).exists).ok();
    let success = false;
    let retries = 10;
    while (!success && retries > 0) {
      if (isDebug()) { console.log(`Click menu entry ${clickSelectorString} ...`); }
      await clickButton(t, clickSelectorString);
      success = await successFunction();
      retries--;
      if (isDebug()) { console.log(`success=${success} retries=${retries}`); }
    }
  }

  public static async clickSettings(t: TestController) {
    await SideMenu.openSideMenuIfClosed(t);
    await clickButton(t, SideMenu.SETTINGS_SELECTOR);
  }

  public static async clickStatus(t: TestController) {
    await this.clickEntry(t, SideMenu.STATUS_SELECTOR, async () => await StatusPage.pageExists(t));
  }

  public static newAppliance(): string {
    return 'a[href="/appliance"]';
  }

  public static async clickNewAppliance(t: TestController) {
    await this.clickEntry(t, SideMenu.newAppliance(), async () => await AppliancePage.pageExists(t));
  }

  public static appliance(id: string): string {
    return `a[href="/appliance/${id}"]`;
  }

  public static async clickAppliance(t: TestController, id: string) {
    await this.clickEntry(t, SideMenu.appliance(id), async () => await AppliancePage.pageExists(t));
  }

  public static meter(id: string): string {
    return `a[href="/meter/${id}"]`;
  }

  public static async clickMeter(t: TestController, id: string) {
    await this.clickEntry(t, SideMenu.meter(id), async () => await MeterPage.pageExists(t));
  }

  public static control(id: string): string {
    return `a[href="/control/${id}"]`;
  }

  public static async clickControl(t: TestController, id: string) {
    await this.clickEntry(t, SideMenu.control(id), async () => await ControlPage.pageExists(t));
  }

  public static schedule(id: string): string {
    return `a[href="/schedule/${id}"]`;
  }

  public static async clickSchedule(t: TestController, id: string) {
    await this.clickEntry(t, SideMenu.schedule(id), async () => await SchedulesPage.pageExists(t));
  }
}
