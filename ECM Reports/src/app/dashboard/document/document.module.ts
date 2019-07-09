import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { Ng2BootstrapModule } from 'ng2-bootstrap';
import { MyDatePickerModule } from 'mydatepicker';
import { DocumentComponent } from './document.component';
import { BlankPageModule } from '../blank-page/blankPage.module';

@NgModule({
    imports: [
    RouterModule,
    FormsModule,ReactiveFormsModule,
    CommonModule,
    Ng2BootstrapModule.forRoot(),
    MyDatePickerModule,
    BlankPageModule
    ],
    declarations: [DocumentComponent],
    exports: [DocumentComponent]
})

export class DocumentModule { }
