import {assertSelect, selectOptionByAttribute, selectorSelectByFormControlName, selectorSelectedByFormControlName} from '../../shared/form';
import {EvHttpControl} from '../../../../../main/angular/src/app/control/evcharger/http/ev-http-control';
import {HttpRead} from '../../../../../main/angular/src/app/http/read/http-read';
import {HttpReadPage} from '../http/http-read.page';
import {HttpWritePage} from '../http/http-write.page';
import {HttpWrite} from '../../../../../main/angular/src/app/http/write/http-write';

export class EvchargerHttpPage {

  public static async setEvChargerHttp(t: TestController, evHttpControl: EvHttpControl) {
    await EvchargerHttpPage.setContentProtocol(t, evHttpControl.contentProtocol);
  }

  public static async assertEvChargerHttp(t: TestController, evHttpControl: EvHttpControl) {
    await EvchargerHttpPage.assertContentProtocol(t, evHttpControl.contentProtocol);
    await EvchargerHttpPage.assertHttpReads(t, evHttpControl.httpReads);
    await EvchargerHttpPage.assertHttpWrites(t, evHttpControl.httpWrites);
  }


  public static async setContentProtocol(t: TestController, contentProtocol: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('contentProtocol'), contentProtocol);
  }

  public static async assertContentProtocol(t: TestController, contentProtocol: string) {
    await assertSelect(t, selectorSelectedByFormControlName('contentProtocol'), contentProtocol);
  }

  public static async setHttpReads(t: TestController, httpReads: HttpRead[]) {
    for (let i = 0; i < httpReads.length; i++) {
      await HttpReadPage.setHttpRead(t, httpReads[i], i, 'app-control-evcharger-http');
    }
  }
  public static async assertHttpReads(t: TestController, httpReads: HttpRead[]) {
    for (let i = 0; i < httpReads.length; i++) {
      await HttpReadPage.assertHttpRead(t, httpReads[i], i, false, 'app-control-evcharger-http',
        'ControlEvchargerComponent.');
    }
  }

  public static async setHttpWrites(t: TestController, httpWrites: HttpWrite[]) {
    for (let i = 0; i < httpWrites.length; i++) {
      await HttpWritePage.setHttpWrite(t, httpWrites[i], i, 'app-control-evcharger-http');
    }
  }
  public static async assertHttpWrites(t: TestController, httpWrites: HttpWrite[]) {
    for (let i = 0; i < httpWrites.length; i++) {
      await HttpWritePage.assertHttpWrite(t, httpWrites[i], i, 'app-control-evcharger-http', 'ControlEvchargerComponent.');
    }
  }
}
