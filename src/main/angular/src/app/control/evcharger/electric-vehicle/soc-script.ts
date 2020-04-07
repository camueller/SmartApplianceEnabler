export class SocScript {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.control.ev.SocScript';
  }
  '@class' = SocScript.TYPE;

  script: string;
  extractionRegex: string;

  public constructor(init?: Partial<SocScript>) {
    Object.assign(this, init);
  }
}
