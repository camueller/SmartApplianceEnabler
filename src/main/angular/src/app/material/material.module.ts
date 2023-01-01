import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDialogModule} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MessageboxComponent} from './messagebox/messagebox.component';
import {TimepickerComponent} from './timepicker/timepicker.component';
import {ReactiveFormsModule} from '@angular/forms';
import {HelpComponent} from './help/help.component';
import {FilenameInputComponent} from './filenameinput/filename-input.component';
import {SafeurlPipe} from '../shared/safe-url.pipe';
import {SharedModule} from '../shared/shared.module';


@NgModule({
  declarations: [
    HelpComponent,
    MessageboxComponent,
    SafeurlPipe,
    TimepickerComponent,
    FilenameInputComponent,
  ],
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    ReactiveFormsModule,
    SharedModule,
  ],
  exports: [
    HelpComponent,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    MessageboxComponent,
    SafeurlPipe,
    TimepickerComponent,
    FilenameInputComponent,
  ],
  providers: [
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {floatLabel: 'always'}},
  ]
})
export class MaterialModule { }
