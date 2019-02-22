import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ControlHttpComponent } from './control-http.component';

describe('ControlHttpComponent', () => {
  let component: ControlHttpComponent;
  let fixture: ComponentFixture<ControlHttpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ControlHttpComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlHttpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
