import {HttpWriteValue} from '../http-write-value/http-write-value';

export class HttpWrite {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.HttpWrite';
  }

  '@class' = HttpWrite.TYPE;
  url: string;
  contentType: string;
  username: string;
  password: string;
  writeValues: HttpWriteValue[];

  public constructor(init?: Partial<HttpWrite>) {
    Object.assign(this, init);
  }
}
