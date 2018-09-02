export class Info {
  version: string;
  buildDate: string;

  public constructor(init?: Partial<Info>) {
    Object.assign(this, init);
  }
}
