import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleRequestRuntimeComponent } from './schedule-request-runtime.component';

describe('ScheduleRequestRuntimeComponent', () => {
  let component: ScheduleRequestRuntimeComponent;
  let fixture: ComponentFixture<ScheduleRequestRuntimeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScheduleRequestRuntimeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleRequestRuntimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
