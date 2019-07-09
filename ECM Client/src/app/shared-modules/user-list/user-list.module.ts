import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpModule} from '@angular/http';
import {
  ButtonModule,
  InputTextModule, TooltipModule, TreeModule
} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedTreeModule} from '../tree-module/tree.module';
import {UserListComponent} from '../../components/generic-components/user-list/user-list.component';

@NgModule({
  declarations: [
    UserListComponent
  ],
  imports: [
    CommonModule,
    HttpModule,
    FormsModule,
    ReactiveFormsModule,
    ButtonModule, SharedTreeModule,
    InputTextModule,
    TreeModule,
    TooltipModule
  ],
  providers: [],
  exports: [UserListComponent]
})
export class SharedUserListModule {

}
