import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpModule} from '@angular/http';
import {
  ButtonModule,
  InputTextModule, TooltipModule
} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {UserListsComponent} from '../../components/generic-components/user-lists/user-lists.component';

@NgModule({
  declarations: [
    UserListsComponent
  ],
  imports: [
    CommonModule,
    HttpModule,
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    TooltipModule
  ],
  exports: [UserListsComponent]
})
export class SharedUserListsModule {

}
