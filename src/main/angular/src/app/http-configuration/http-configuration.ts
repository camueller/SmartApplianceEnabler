export class HttpConfiguration {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.HttpConfiguration';
  }

  '@class' = HttpConfiguration.TYPE;
  contentType: string;
  username: string;
  password: string;

  public constructor(init?: Partial<HttpConfiguration>) {
    Object.assign(this, init);
  }
}
