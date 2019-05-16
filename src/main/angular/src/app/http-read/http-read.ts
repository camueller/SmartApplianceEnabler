import {HttpReadValue} from '../http-read-value/http-read-value';

export class HttpRead {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.HttpRead';
  }

  '@class' = HttpRead.TYPE;
  url: string;
  contentType: string;
  username: string;
  password: string;
  readValues: HttpReadValue[];

  public constructor(init?: Partial<HttpRead>) {
    Object.assign(this, init);
  }
}
