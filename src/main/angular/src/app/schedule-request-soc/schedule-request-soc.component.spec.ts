import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleRequestSocComponent } from './schedule-request-soc.component';

describe('ScheduleRequestSocComponent', () => {
  let component: ScheduleRequestSocComponent;
  let fixture: ComponentFixture<ScheduleRequestSocComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScheduleRequestSocComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleRequestSocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
