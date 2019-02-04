import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusEvchargerEditComponent } from './status-evcharger-edit.component';

describe('StatusEvchargerEditComponent', () => {
  let component: StatusEvchargerEditComponent;
  let fixture: ComponentFixture<StatusEvchargerEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StatusEvchargerEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusEvchargerEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
