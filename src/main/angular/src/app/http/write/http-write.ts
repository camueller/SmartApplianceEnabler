import {HttpWriteValue} from '../write-value/http-write-value';

export class HttpWrite {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.HttpWrite';
  }

  '@class' = HttpWrite.TYPE;
  url: string;
  writeValues: HttpWriteValue[];

  public constructor(init?: Partial<HttpWrite>) {
    Object.assign(this, init);
  }

  public static createWithSingleChild() {
    return new HttpWrite({writeValues: [HttpWriteValue.createWithSingleChild()]});
  }
}
