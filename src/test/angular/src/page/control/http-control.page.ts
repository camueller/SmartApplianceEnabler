import {ControlPage} from './control.page';
import {ControlValueName} from '../../../../../main/angular/src/app/control/control-value-name';
import {HttpWritePage} from '../http/http-write.page';
import {HttpSwitch} from '../../../../../main/angular/src/app/control/http/http-switch';

export class HttpControlPage extends ControlPage {

  private static selectorPrefix = 'app-control-http';
  private static i18nPrefix = 'ControlHttpComponent.';

  public static async setHttpSwitch(t: TestController, httpSwitch: HttpSwitch) {
    await HttpControlPage.setType(t, HttpSwitch.TYPE);

    const onHttpWrite = httpSwitch.httpWrites.find(
      httpWrite => httpWrite.writeValues.find(httpWriteValue => httpWriteValue.name === ControlValueName.On));
    let httpWriteIndex = 0;
    await HttpWritePage.setHttpWrite(t, onHttpWrite, httpWriteIndex, this.selectorPrefix);
    await HttpWritePage.setHttpWriteValue(t, onHttpWrite.writeValues[0], httpWriteIndex, this.selectorPrefix);

    const offHttpWrite = httpSwitch.httpWrites.find(
    httpWrite => httpWrite.writeValues.find(httpWriteValue => httpWriteValue.name === ControlValueName.Off));
    httpWriteIndex = 1;
    await HttpWritePage.clickAddHttpWrite(t, this.selectorPrefix, 'ControlHttpComponent__addHttpWrite');
    await HttpWritePage.setHttpWrite(t, offHttpWrite, httpWriteIndex, this.selectorPrefix);
    await HttpWritePage.setHttpWriteValue(t, offHttpWrite.writeValues[0], httpWriteIndex, this.selectorPrefix);
  }
  public static async assertHttpSwitch(t: TestController, httpSwitch: HttpSwitch) {
    await HttpControlPage.assertType(t, HttpSwitch.TYPE);

    const onHttpWrite = httpSwitch.httpWrites.find(
      httpWrite => httpWrite.writeValues.find(httpWriteValue => httpWriteValue.name === ControlValueName.On));
    let httpWriteIndex = 0;
    await HttpWritePage.assertHttpWrite(t, onHttpWrite, httpWriteIndex, this.selectorPrefix);
    await HttpWritePage.assertHttpWriteValue(t, onHttpWrite.writeValues[0], httpWriteIndex, this.selectorPrefix,
      HttpControlPage.i18nPrefix);

    const offHttpWrite = httpSwitch.httpWrites.find(
      httpWrite => httpWrite.writeValues.find(httpWriteValue => httpWriteValue.name === ControlValueName.Off));
    if (offHttpWrite) {
      httpWriteIndex = 1;
      await HttpWritePage.assertHttpWrite(t, offHttpWrite, httpWriteIndex, this.selectorPrefix);
      await HttpWritePage.assertHttpWriteValue(t, offHttpWrite.writeValues[0], httpWriteIndex, this.selectorPrefix,
        HttpControlPage.i18nPrefix);
    }
  }
}
