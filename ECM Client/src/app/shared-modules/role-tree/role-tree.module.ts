import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpModule} from '@angular/http';
import {ButtonModule, TooltipModule, TreeModule} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RoleTreeComponent} from '../../components/generic-components/role-tree/role-tree.component';
import {SharedHTMLPipeModule} from "../safe-html-pipe/safe-html-pipe";

@NgModule({
  declarations: [
    RoleTreeComponent
  ],
  imports: [
    CommonModule,
    HttpModule,
    ButtonModule,
    TreeModule,
    TooltipModule,
    SharedHTMLPipeModule
  ],
  providers: [],
  exports: [RoleTreeComponent]
})
export class SharedRoleTreeModule {

}
