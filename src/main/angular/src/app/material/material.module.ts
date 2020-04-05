import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule, MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule, MatTooltipModule, MatOptionModule, MatSelectModule, MatCheckboxModule} from '@angular/material';
import {MatListModule} from '@angular/material/list';
import {MatCardModule} from '@angular/material/card';
import {MessageboxComponent} from './messagebox/messagebox.component';
import {FlexModule} from '@angular/flex-layout';


@NgModule({
  declarations: [
    MessageboxComponent,
  ],
  imports: [
    CommonModule,
    FlexModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatOptionModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
  ],
  exports: [
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
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
  ],
  providers: [
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {floatLabel: 'always'}},
  ]
})
export class MaterialModule { }
