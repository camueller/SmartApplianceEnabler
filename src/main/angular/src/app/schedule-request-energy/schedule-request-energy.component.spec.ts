import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleRequestEnergyComponent } from './schedule-request-energy.component';

describe('ScheduleRequestEnergyComponent', () => {
  let component: ScheduleRequestEnergyComponent;
  let fixture: ComponentFixture<ScheduleRequestEnergyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScheduleRequestEnergyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleRequestEnergyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
