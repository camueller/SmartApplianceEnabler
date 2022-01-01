import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatOptionModule} from '@angular/material/core';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatListModule} from '@angular/material/list';
import {MatCardModule} from '@angular/material/card';
import {MessageboxComponent} from './messagebox/messagebox.component';
import {FlexModule} from '@angular/flex-layout';
import {TimepickerComponent} from './timepicker/timepicker.component';
import {ReactiveFormsModule} from '@angular/forms';
import {HelpComponent} from './help/help.component';
import {SafeurlPipe} from './safe-url.pipe';
import {FilenameInputComponent} from './filenameinput/filename-input.component';
import {MatDialogModule} from '@angular/material/dialog';


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
    FlexModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatOptionModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    ReactiveFormsModule,
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
    MatOptionModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    MessageboxComponent,
    TimepickerComponent,
    FilenameInputComponent,
  ],
  providers: [
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {floatLabel: 'always'}},
  ]
})
export class MaterialModule { }
