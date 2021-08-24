export class HttpWriteValue {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.HttpWriteValue';
  }

  '@class' = HttpWriteValue.TYPE;
  name: string;
  value: string;
  factorToValue: number;
  method: string;

  public constructor(init?: Partial<HttpWriteValue>) {
    Object.assign(this, init);
  }

  public static createWithSingleChild() {
    return new HttpWriteValue({method: 'GET'});
  }
}
