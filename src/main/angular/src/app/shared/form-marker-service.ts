import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';

// TODO remove, form can be accessed directly (FormGroupDirective)
@Injectable()
export class FormMarkerService {

  dirty: Subject<any> = new Subject();

  markDirty() {
    this.dirty.next();
  }
}
