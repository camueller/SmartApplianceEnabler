import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {ApplianceHeader} from '../../appliance/appliance-header';
import {AppliancesReloadService} from '../../appliance/appliances-reload-service';
import {ApplianceService} from '../../appliance/appliance.service';
import {TranslateService} from '@ngx-translate/core';
import {ApplianceType} from '../../appliance/appliance-type';

@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss']
})
export class SidenavComponent implements OnInit {

  @Output()
  sidenavClose = new EventEmitter();
  applianceHeaders: ApplianceHeader[];
  readonly typePrefix = 'ApplianceComponent.type.';
  translatedTypes = {};
  translatedStrings: { [key: string]: string } = {};
  controlKey = 'AppComponent.control';
  wallboxKey = 'ControlEvchargerComponent.heading';

  constructor(
    private applianceService: ApplianceService,
    private appliancesReloadService: AppliancesReloadService,
    private translate: TranslateService,
  ) {
  }

  ngOnInit() {
    this.loadAppliances();
    this.appliancesReloadService.triggered.subscribe(() => {
      this.loadAppliances();
    });
    this.translate.get([
      this.controlKey,
      this.wallboxKey,
    ]).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  public onSidenavClose(force?: boolean) {
    this.sidenavClose.emit(force);
  }

  get hasAppliances() {
    return this.applianceHeaders && this.applianceHeaders.length > 0;
  }

  loadAppliances() {
    this.applianceService.getApplianceHeaders().subscribe(applianceHeaders => {
      this.applianceHeaders = applianceHeaders;

      const types = [];
      this.applianceHeaders.forEach(applianceHeader => types.push(this.typePrefix + applianceHeader.type));
      if (types.length > 0) {
        this.translate.get(types).subscribe(translatedTypes => this.translatedTypes = translatedTypes);
      }
    });
  }

  getTranslatedType(type: string): string {
    if (this.translatedTypes != null) {
      return this.translatedTypes[this.typePrefix + type];
    }
    return '';
  }

  getTranslatedControlLabel(type: string) {
    return type === ApplianceType.EV_CHARGER ? this.translatedStrings[this.wallboxKey] : this.translatedStrings[this.controlKey];
  }
}
