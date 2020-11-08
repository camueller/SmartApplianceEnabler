export class SocScript {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.control.ev.SocScript';
  }
  '@class' = SocScript.TYPE;

  script: string;
  extractionRegex: string;
  updateAfterIncrease: number;
  updateAfterSeconds: number;

  public constructor(init?: Partial<SocScript>) {
    Object.assign(this, init);
  }
}
