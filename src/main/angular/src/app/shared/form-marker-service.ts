import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';

@Injectable()
export class FormMarkerService {

  dirty: Subject<any> = new Subject();

  markDirty() {
    this.dirty.next();
  }
}
