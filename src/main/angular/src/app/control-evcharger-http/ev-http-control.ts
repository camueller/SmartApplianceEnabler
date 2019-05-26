import {HttpRead} from '../http-read/http-read';
import {HttpWrite} from '../http-write/http-write';

export class EvHttpControl {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.EVHttpControl';
  }

  '@class' = EvHttpControl.TYPE;
  contentProtocol: string;
  httpReads: HttpRead[];
  httpWrites: HttpWrite[];

  public constructor(init?: Partial<EvHttpControl>) {
    Object.assign(this, init);
  }

}
