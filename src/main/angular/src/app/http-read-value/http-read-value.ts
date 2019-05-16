export class HttpReadValue {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.HttpReadValue';
  }

  '@class' = HttpReadValue.TYPE;
  name: string;
  data: string;
  path: string;
  extractionRegex: string;
  factorToValue: number;

  public constructor(init?: Partial<HttpReadValue>) {
    Object.assign(this, init);
  }
}
