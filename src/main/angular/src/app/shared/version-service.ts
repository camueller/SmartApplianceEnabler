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

export interface AvailableVersion {
  version: string;
  prerelease: boolean;
}

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

  public getAvailableVersion(): Observable<AvailableVersion[] | undefined> {
      const resultSubject = new BehaviorSubject(undefined);
      this.getCurrentVersion().subscribe(async (currentVersion) => {
        if (this.availableVersion) {
          resultSubject.next(this.availableVersion);
          return;
        }
        const releases = await this.octo.repos.listReleases({owner: 'camueller', repo: 'SmartApplianceEnabler'});
        const orderedReleasesMostRecentFirst = releases.data.sort((release1, release2) =>
          release1.published_at < release2.published_at ? 1 : release1.published_at > release2.published_at ? -1 : 0);
        const availableVersions = [];
        if (orderedReleasesMostRecentFirst && orderedReleasesMostRecentFirst.length > 0) {
          const currentVersionRelease = orderedReleasesMostRecentFirst.find(release => release.tag_name === currentVersion?.version);
          const mostRecentStableRelease = orderedReleasesMostRecentFirst.find(release => !release.prerelease);
          const mostRecentRelease = orderedReleasesMostRecentFirst[0];
          if (currentVersionRelease && mostRecentStableRelease && mostRecentStableRelease.tag_name > currentVersion?.version) {
            availableVersions.push({
              version: mostRecentStableRelease.tag_name,
              prerelease: mostRecentStableRelease.prerelease
            });
          }
          if (currentVersion?.version !== mostRecentRelease.tag_name) {
            availableVersions.push({
              version: orderedReleasesMostRecentFirst[0].tag_name,
              prerelease: orderedReleasesMostRecentFirst[0].prerelease
            });
          }
        }
        resultSubject.next(availableVersions ? availableVersions : undefined);
      });
      return resultSubject;
  }
}
