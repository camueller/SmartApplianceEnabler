import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusEvchargerViewComponent } from './status-evcharger-view.component';

describe('StatusEvchargerViewComponent', () => {
  let component: StatusEvchargerViewComponent;
  let fixture: ComponentFixture<StatusEvchargerViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StatusEvchargerViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusEvchargerViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
