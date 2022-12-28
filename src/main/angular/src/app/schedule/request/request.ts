export class Request {
  enabled: boolean;

  public constructor(init?: Partial<Request>) {
    Object.assign(this, init);
  }
}
