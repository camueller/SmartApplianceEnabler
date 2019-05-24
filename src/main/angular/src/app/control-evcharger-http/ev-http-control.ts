import {HttpRead} from '../http-read/http-read';

export class EvHttpControl {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.http.EVHttpControl';
  }

  '@class' = EvHttpControl.TYPE;
  httpReads: HttpRead[];

  public constructor(init?: Partial<EvHttpControl>) {
    Object.assign(this, init);
  }

}
