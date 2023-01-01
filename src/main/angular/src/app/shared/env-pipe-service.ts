import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable()
export class EnvPipeService {

  private matTooltipDisabled = new BehaviorSubject(false);
  private forceSideMenuStayOpen = new BehaviorSubject(false);

  public getMatTooltipDisabled(): Observable<boolean> {
    return this.matTooltipDisabled;
  }

  public setMatTooltipDisabled(matTooltipDisabled: boolean) {
    this.matTooltipDisabled.next(matTooltipDisabled);
  }

  public getForceSideMenuStayOpen(): Observable<boolean> {
    return this.forceSideMenuStayOpen;
  }

  public setForceSideMenuStayOpen(forceSideMenuStayOpen: boolean) {
    this.forceSideMenuStayOpen.next(forceSideMenuStayOpen);
  }
}
