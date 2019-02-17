import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ControlEvchargerComponent } from './control-evcharger.component';

describe('ControlEvchargerComponent', () => {
  let component: ControlEvchargerComponent;
  let fixture: ComponentFixture<ControlEvchargerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ControlEvchargerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlEvchargerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
