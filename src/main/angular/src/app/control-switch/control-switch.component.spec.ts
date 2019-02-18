import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ControlSwitchComponent } from './control-switch.component';

describe('ControlSwitchComponent', () => {
  let component: ControlSwitchComponent;
  let fixture: ComponentFixture<ControlSwitchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ControlSwitchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlSwitchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
