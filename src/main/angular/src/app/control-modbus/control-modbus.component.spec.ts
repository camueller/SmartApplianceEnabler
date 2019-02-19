import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ControlModbusComponent } from './control-modbus.component';

describe('ControlModbusComponent', () => {
  let component: ControlModbusComponent;
  let fixture: ComponentFixture<ControlModbusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ControlModbusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlModbusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
