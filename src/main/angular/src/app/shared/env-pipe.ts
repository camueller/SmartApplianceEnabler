import {Injectable, Pipe, PipeTransform} from '@angular/core';
import {environment} from '../../environments/environment';
import {LanguageService} from './language-service';
import {BehaviorSubject, Observable} from 'rxjs';
import {EnvPipeService} from './env-pipe-service';

@Pipe({name: 'env'})
export class EnvPipe implements PipeTransform {
  private matTooltipDisabled = false;

  constructor(private envPipeService: EnvPipeService) {
    this.envPipeService.getMatTooltipDisabled().subscribe(matTooltipDisabled => {
      this.matTooltipDisabled = matTooltipDisabled;
    });
  }

  transform(variable: string): any {
    if(variable === 'matTooltipDisabled') {
      return this.matTooltipDisabled;
    }
    return undefined;
  }
}
