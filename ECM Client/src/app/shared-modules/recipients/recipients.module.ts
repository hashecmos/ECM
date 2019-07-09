import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpModule} from '@angular/http';
import {
  AutoCompleteModule,
  ButtonModule, DropdownModule,
  InputTextModule, TooltipModule,SpinnerModule
} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedTreeModule} from '../tree-module/tree.module';
import {RecipientsComponent} from '../../components/generic-components/recipients/recipients.component';
import {SharedUserListModule} from '../user-list/user-list.module';
import {
  MatTabsModule
} from '@angular/material';
import {SharedUserListsModule} from '../user-lists/user-lists.module';
import {SharedRoleTreeModule} from '../role-tree/role-tree.module';
import {SplitPaneModule} from "ng2-split-pane/lib/ng2-split-pane";

@NgModule({
  declarations: [
    RecipientsComponent
  ],
  imports: [
    CommonModule,
    HttpModule,
    DropdownModule,
    FormsModule,
    ReactiveFormsModule,
    ButtonModule, SharedTreeModule,SplitPaneModule,
    InputTextModule,
    MatTabsModule,
    SharedUserListModule,
    AutoCompleteModule,
    TooltipModule,
    SharedUserListsModule,
    SharedRoleTreeModule,SpinnerModule
  ],
  providers: [],
  exports: [RecipientsComponent]
})
export class SharedRecipientsModule {

}
