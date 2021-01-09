/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import {Injectable} from '@angular/core';
import {Info} from '../settings/info';
import {SaeService} from './sae-service';
import {map} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {Octokit} from '@octokit/rest';

@Injectable()
export class VersionService {

  private currentVersion: Info;
  private availableVersion: string;
  private octo = new Octokit();

  constructor(protected http: HttpClient) {
  }

  public getCurrentVersion(): Observable<Info> {
    if (this.currentVersion) {
      return new BehaviorSubject(this.currentVersion);
    }
    return this.http.get(`${SaeService.API}/info`).pipe(map(info => {
      this.currentVersion = new Info(info);
      // this.currentVersion.version = '1.4.19';
      return this.currentVersion;
    }));
  }

  /**
   * The most recent available version is:
   * - the version of the most recent release NOT being a pre-release if the current version is NOT a pre-release
   * - the version of the most recent release being a pre-release if the current version is a pre-release
   * If the current version differs from the most recent available version the latter is returned, otherweise undefined.
   */
  public getAvailableVersion(): Observable<string | undefined> {
      const resultSubject = new BehaviorSubject(undefined);
      this.getCurrentVersion().subscribe(async (currentVersion) => {
        if (this.availableVersion) {
          resultSubject.next(this.availableVersion);
          return;
        }
        const releases = await this.octo.repos.listReleases({owner: 'camueller', repo: 'SmartApplianceEnabler'});
        const currentRelease = releases.data.find(release => release.tag_name === currentVersion.version);
        let availableVersion;
        // Previous pre-releases have been deleted => if we don't find a release for the current version it is a pre-release
        if (!currentRelease || currentRelease.prerelease) {
          availableVersion = releases.data.find(release => release.prerelease)?.tag_name;
        } else {
          availableVersion = releases.data.find(release => !release.prerelease)?.tag_name;
        }
        resultSubject.next(availableVersion && availableVersion !== currentVersion.version ? availableVersion : undefined);
      });
      return resultSubject;
  }
}
