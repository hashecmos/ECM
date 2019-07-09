import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MyDatePickerModule } from 'mydatepicker';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { Ng2BootstrapModule } from 'ng2-bootstrap';
import { BlankPageComponent } from './blankPage.component';
import { TreeModule } from 'angular-tree-component';
import {TreeComponent} from '../../shared/index';

@NgModule({
    imports: [
    RouterModule,
    FormsModule,ReactiveFormsModule,
    CommonModule,
    Ng2BootstrapModule.forRoot(),
    MyDatePickerModule,
    TreeModule
    ],
    declarations: [BlankPageComponent, TreeComponent],
    exports: [BlankPageComponent, TreeComponent]
})

export class BlankPageModule { }
