import {MeterPage} from './meter.page';
import {HttpElectricityMeter} from '../../../../../main/angular/src/app/meter-http/http-electricity-meter';
import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {HttpReadPage} from '../http/http-read.page';

export class HttpMeterPage extends MeterPage {

  private static selectorPrefix = 'app-meter-http';

  public static async setHttpElectricityMeter(t: TestController, httpElectricityMeter: HttpElectricityMeter) {
    await HttpMeterPage.setType(t, HttpElectricityMeter.TYPE);

    const powerHttpRead = httpElectricityMeter.httpReads.find(
      httpRead => httpRead.readValues.find(httpReadValue => httpReadValue.name === MeterValueName.Power));
    let httpReadIndex = 0;
    await HttpReadPage.setHttpRead(t, powerHttpRead, httpReadIndex, this.selectorPrefix);
    await HttpReadPage.setHttpReadValue(t, powerHttpRead.readValues[0], httpReadIndex, this.selectorPrefix);

    const energyHttpRead = httpElectricityMeter.httpReads.find(
      httpRead => httpRead.readValues.find(httpReadValue => httpReadValue.name === MeterValueName.Energy));
    if (energyHttpRead) {
      httpReadIndex = 1;
      await HttpReadPage.clickAddHttpRead(t, this.selectorPrefix);
      await HttpReadPage.setHttpRead(t, energyHttpRead, httpReadIndex, this.selectorPrefix);
      await HttpReadPage.setHttpReadValue(t, energyHttpRead.readValues[0], httpReadIndex, this.selectorPrefix);
    }
  }
  public static async assertHttpElectricityMeter(t: TestController, httpElectricityMeter: HttpElectricityMeter) {
    await HttpMeterPage.assertType(t, HttpElectricityMeter.TYPE);

    const powerHttpRead = httpElectricityMeter.httpReads.find(
      httpRead => httpRead.readValues.find(httpReadValue => httpReadValue.name === MeterValueName.Power));
    let httpReadIndex = 0;
    await HttpReadPage.assertHttpRead(t, powerHttpRead, httpReadIndex, this.selectorPrefix);
    await HttpReadPage.assertHttpReadValue(t, powerHttpRead.readValues[0], httpReadIndex, this.selectorPrefix);

    const energyHttpRead = httpElectricityMeter.httpReads.find(
      httpRead => httpRead.readValues.find(httpReadValue => httpReadValue.name === MeterValueName.Energy));
    if (energyHttpRead) {
      httpReadIndex = 1;
      await HttpReadPage.assertHttpRead(t, energyHttpRead, httpReadIndex, this.selectorPrefix);
      await HttpReadPage.assertHttpReadValue(t, energyHttpRead.readValues[0], httpReadIndex, this.selectorPrefix);
    }
  }

}
