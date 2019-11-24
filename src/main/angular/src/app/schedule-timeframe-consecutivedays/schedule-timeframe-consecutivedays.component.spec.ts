import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleTimeframeConsecutivedaysComponent } from './schedule-timeframe-consecutivedays.component';

describe('ScheduleTimeframeConsecutivedaysComponent', () => {
  let component: ScheduleTimeframeConsecutivedaysComponent;
  let fixture: ComponentFixture<ScheduleTimeframeConsecutivedaysComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScheduleTimeframeConsecutivedaysComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleTimeframeConsecutivedaysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
