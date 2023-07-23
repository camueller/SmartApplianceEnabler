export class SocScript {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.control.ev.SocScript';
  }
  '@class' = SocScript.TYPE;

  script: string;
  extractionRegex: string;
  pluginStatusExtractionRegex: string;
  pluginTimeExtractionRegex: string;
  latitudeExtractionRegex: string;
  longitudeExtractionRegex: string;
  updateAfterIncrease: number;
  updateAfterSeconds: number;
  timeoutSeconds: number;

  public constructor(init?: Partial<SocScript>) {
    Object.assign(this, init);
  }
}
