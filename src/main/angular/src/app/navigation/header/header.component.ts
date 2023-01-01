import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {AvailableVersion, VersionService} from '../../shared/version-service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @Output()
  public sidenavToggle = new EventEmitter();
  version: string;
  availableVersions: AvailableVersion[];

  constructor(
    private versionService: VersionService
  ) { }

  ngOnInit() {
    this.versionService.getCurrentVersion().subscribe(info => {
      this.version = info.version;
      this.versionService.getAvailableVersion().subscribe(availableVersions => {
        this.availableVersions = availableVersions;
      });
    });
  }

  public get betaUpdateVersion() {
    return this.availableVersions && this.availableVersions.find(version => version.prerelease)?.version;
  }

  public get stableUpdateVersion() {
    return this.availableVersions && this.availableVersions.find(version => !version.prerelease)?.version;
  }

  public get betaUpdateUrl() {
    return `https://github.com/camueller/SmartApplianceEnabler/releases/tag/${this.betaUpdateVersion}`;
  }

  public get stableUpdateUrl() {
    return `https://github.com/camueller/SmartApplianceEnabler/releases/tag/${this.stableUpdateVersion}`;
  }

  public onToggleSidenav = () => {
    this.sidenavToggle.emit();
  }

  public get isHigher() {
    return !this.isHigheest && (this.betaUpdateVersion || this.stableUpdateVersion);
  }

  public get isHigheest() {
    return !!this.betaUpdateVersion && !!this.stableUpdateVersion;
  }
}
