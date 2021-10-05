export class HttpReadValue {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.HttpReadValue';
  }

  '@class' = HttpReadValue.TYPE;
  name: string;
  method?: string;
  data?: string;
  path?: string;
  extractionRegex?: string;
  factorToValue?: number;

  public constructor(init?: Partial<HttpReadValue>) {
    Object.assign(this, init);
  }

  public static createWithSingleChild() {
    return new HttpReadValue({method: 'GET'});
  }
}
