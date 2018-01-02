import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import {convertToParamMap, Data, ParamMap} from '@angular/router';
import {Injectable} from '@angular/core';

@Injectable()
export class ActivatedRouteStub {

  // ActivatedRoute.paramMap is Observable
  private paramSubject = new BehaviorSubject(convertToParamMap(this.testParamMap));
  paramMap = this.paramSubject.asObservable();

  // Test parameters
  private _testParamMap: ParamMap;
  get testParamMap() { return this._testParamMap; }
  set testParamMap(params: {}) {
    this._testParamMap = convertToParamMap(params);
    this.paramSubject.next(this._testParamMap);
  }

  // ActivatedRoute.snapshot.paramMap
  get snapshot() {
    return { paramMap: this.testParamMap };
  }


  // ActivatedRoute.data is Observable
  private dataSubject = new BehaviorSubject(this.testData);

  // Test data
  private _testData: Data;
  get testData() { return this._testData; }
  set testData(data: {}) {
    this._testData = data;
    this.dataSubject.next(this._testData);
  }

  // ActivatedRoute.snapshot.data
  get data() {
    return this.dataSubject;
  }
}
