import {async, ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';
import {ControlHttpComponent} from './control-http.component';
import {Component, EventEmitter, NO_ERRORS_SCHEMA, Output, ViewChild} from '@angular/core';
import {HttpSwitch} from './http-switch';
import {click, createComponentAndConfigure, debugElementByCss, importsFormsAndTranslate, providers} from '../../shared/test-util';
import {FormGroup} from '@angular/forms';
import {HttpRead} from '../../http/read/http-read';
import {HttpWriteValue} from '../../http/write-value/http-write-value';
import {HttpConfiguration} from '../../http/configuration/http-configuration';
import {HttpWrite} from '../../http/write/http-write';

const removeHttpWrite = new EventEmitter<any>();
const httpConfigurationUpdateModelFromFormMock = jest.fn();
const httpWriteUpdateModelFromFormMock = jest.fn();
const httpReadUpdateModelFromFormMock = jest.fn();

@Component({selector: 'app-http-configuration', template: ''})
class HttpConfigurationStubComponent {
  updateModelFromForm(): HttpConfiguration | undefined {
    return httpConfigurationUpdateModelFromFormMock();
  }
}

@Component({selector: 'app-http-write', template: ''})
class HttpWriteStubComponent {
  @Output()
  remove = removeHttpWrite;

  updateModelFromForm(): HttpWrite | undefined {
    return httpWriteUpdateModelFromFormMock();
  }
}

@Component({selector: 'app-http-read', template: ''})
class HttpReadStubComponent {
  updateModelFromForm(): HttpRead | undefined {
    return httpReadUpdateModelFromFormMock();
  }
}

@Component({
  template: `
    <form [formGroup]="form">
        <app-control-http
            [httpSwitch]="httpSwitch"
            [applianceId]="applianceId"
        ></app-control-http>
    </form>`
})
class ControlHttpTestHostComponent {
  @ViewChild(ControlHttpComponent, { static: true }) testComponent;
  httpSwitch = undefined;
  applianceId = 'F-00000000001-00';
  form = new FormGroup({});
}

describe('ControlHttpComponent', () => {
  const ADD_HTTPWRITE_BUTTON = 'button.addHttpWrite';
  const READ_CONTROL_STATE_CHECKBOX = 'input.readControlState';

  let component: ControlHttpComponent;
  let hostComponent: ControlHttpTestHostComponent;
  let fixture: ComponentFixture<ControlHttpTestHostComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ControlHttpComponent,
        ControlHttpTestHostComponent,
        HttpConfigurationStubComponent,
        HttpReadStubComponent,
        HttpWriteStubComponent
      ],
      imports: importsFormsAndTranslate(),
      providers: providers(),
      schemas: [ NO_ERRORS_SCHEMA ]
    });
    fixture = createComponentAndConfigure(ControlHttpTestHostComponent);
    hostComponent = fixture.componentInstance;
    component = hostComponent.testComponent;

    fixture.detectChanges();
    // fixture.whenStable().then(() => {
    //   console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    // });
  }));

  describe('Initially', () => {

    it('a Http switch will be created if none is passed in', () => {
      expect(component.httpSwitch).toBeTruthy();
    });

    it('the HttpSwitch can be passed in', async( () => {
      hostComponent.httpSwitch = new HttpSwitch();
      fixture.detectChanges();
      expect(component.httpSwitch).toBeTruthy();
    }));

    it('the HttpSwitch contains one HttpWrite', async( () => {
      expect(component.httpSwitch.httpWrites.length).toBe(1);
    }));

    it('the readControlState checkbox is not checked', () => {
      expect(debugElementByCss(fixture, READ_CONTROL_STATE_CHECKBOX).nativeElement.checked).toBeFalsy();
    });

    it('the HttpSwitch contains no HttpRead', async( () => {
      expect(component.httpSwitch.httpRead).toBeUndefined();
    }));
  });

  describe('HttpWrite', () => {
   it('should handle "remove" event', () => {
     removeHttpWrite.emit();
     expect(component.httpSwitch.httpWrites.length).toBe(0);
     expect(component.httpWritesFormArray.length).toBe(0);
     fixture.whenStable().then(() => {
       expect(component.form.dirty).toBeTruthy();
     });
   });
  });

  describe('Button "Weitere URL"', () => {

    it('exists and is enabled', async( () => {
      const button = debugElementByCss(fixture, ADD_HTTPWRITE_BUTTON);
      expect(button.nativeElement.innerHTML).toBe('Weitere URL');
      expect(button.nativeElement.disabled).toBeFalsy();
    }));

    describe('with one existing HttpWrite', () => {
      it('another HttpRead can be added if it has one HttpReadValue', async( () => {
        click(debugElementByCss(fixture, ADD_HTTPWRITE_BUTTON));
        expect(component.httpSwitch.httpWrites.length).toBe(2);
      }));

      it('no HttpRead can be added if the existing HttpWrite contains two HttpWriteValue', async( () => {
        component.httpSwitch.httpWrites[0].writeValues.push(new HttpWriteValue());
        fixture.detectChanges();
        expect(debugElementByCss(fixture, ADD_HTTPWRITE_BUTTON)).toBeFalsy();
      }));
    });

    describe('with two existing HttpWrite', () => {
      it('no HttpRead can be added if the existing HttpWrite contains two HttpWriteValue', async( () => {
        component.addHttpWrite();
        fixture.detectChanges();
        expect(debugElementByCss(fixture, ADD_HTTPWRITE_BUTTON)).toBeFalsy();
      }));
    });
  });

  describe('HttpRead', () => {

    beforeEach(() => {
      expect(component.form.dirty).toBeFalsy();
      debugElementByCss(fixture, READ_CONTROL_STATE_CHECKBOX).nativeElement.click();
      fixture.detectChanges();
    });

    it('can be enabled', () => {
      expect(debugElementByCss(fixture, READ_CONTROL_STATE_CHECKBOX).nativeElement.checked).toBeTruthy();
    });

    it('contains a HttpRead', () => {
      expect(debugElementByCss(fixture, 'app-http-read')).toBeTruthy();
    });
  });

  describe('updateModelFromForm', () => {
    describe('returns HttpSwitch', () => {
      let httpSwitch: HttpSwitch;
      const httpConfigurationContentType = 'httpConfiguration.contentType';
      const httpWriteUrl = 'httpWrite.url';
      const httpReadUrl = 'httpRead.url';

      beforeEach(fakeAsync(() => {
        httpConfigurationUpdateModelFromFormMock.mockReturnValue(
          {contentType: httpConfigurationContentType} as HttpConfiguration);
        httpWriteUpdateModelFromFormMock.mockReturnValue({url: httpWriteUrl, writeValues: []} as HttpWrite);
        httpReadUpdateModelFromFormMock.mockReturnValue({url: httpReadUrl, readValues: []} as HttpRead);
        httpSwitch = component.updateModelFromForm();
      }));

      it('with HttpConfiguration', () => {
        expect(httpSwitch.httpConfiguration.contentType).toBe(httpConfigurationContentType);
      });

      it('with HttpWrite', () => {
        expect(httpSwitch.httpWrites[0].url).toBe(httpWriteUrl);
      });

      it('with HttpRead', () => {
        debugElementByCss(fixture, READ_CONTROL_STATE_CHECKBOX).nativeElement.click();
        fixture.detectChanges();
        httpSwitch = component.updateModelFromForm();
        expect(httpSwitch.httpRead.url).toBe(httpReadUrl);
      });
    });

    describe('returns undefined', () => {
      it('if form values have not been changed', () => {
        httpConfigurationUpdateModelFromFormMock.mockReturnValue(undefined);
        httpWriteUpdateModelFromFormMock.mockReturnValue(undefined);
        httpReadUpdateModelFromFormMock.mockReturnValue(undefined);
        expect(component.updateModelFromForm()).toBe(undefined);
      });
    });

  });
});
