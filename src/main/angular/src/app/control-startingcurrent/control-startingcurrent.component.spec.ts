import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ControlStartingcurrentComponent } from './control-startingcurrent.component';

describe('ControlStartingcurrentComponent', () => {
  let component: ControlStartingcurrentComponent;
  let fixture: ComponentFixture<ControlStartingcurrentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ControlStartingcurrentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlStartingcurrentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
