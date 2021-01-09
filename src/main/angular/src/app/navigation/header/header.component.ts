import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {VersionService} from '../../shared/version-service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @Output()
  public sidenavToggle = new EventEmitter();
  version: string;
  availableVersion: string;

  constructor(
    private versionService: VersionService
  ) { }

  ngOnInit() {
    this.versionService.getCurrentVersion().subscribe(info => {
      this.version = info.version;
      this.versionService.getAvailableVersion().subscribe(availableVersion => {
        this.availableVersion = availableVersion;
        console.log('HeaderComponent availableVersion=', availableVersion);
      });
    });
  }

  public get updateUrl() {
    return `https://github.com/camueller/SmartApplianceEnabler/releases/tag/${this.availableVersion}`;
  }

  public onToggleSidenav = () => {
    this.sidenavToggle.emit();
  }
}
