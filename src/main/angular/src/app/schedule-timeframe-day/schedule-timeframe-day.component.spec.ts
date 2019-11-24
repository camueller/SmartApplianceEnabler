import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleTimeframeDayComponent } from './schedule-timeframe-day.component';

describe('ScheduleTimeframeDayComponent', () => {
  let component: ScheduleTimeframeDayComponent;
  let fixture: ComponentFixture<ScheduleTimeframeDayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScheduleTimeframeDayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleTimeframeDayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
