import { NgModule } from '@angular/core';
import {APP_BASE_HREF, CommonModule} from '@angular/common';
import {ReportRoute} from "./report.routes";
import {ReportComponent} from "./report.component";
import {
  AccordionModule, AutoCompleteModule, ButtonModule, CalendarModule, CheckboxModule, DataTableModule, DropdownModule,
  InputTextModule,
  MenubarModule,
  SplitButtonModule,
  TooltipModule,
  TreeModule
} from "primeng/primeng";
import {ReportsRoleTreeComponent} from "../../components/generic-components/reports-role-tree/reports-role-tree.component";
import {SharedHTMLPipeModule} from "../../shared-modules/safe-html-pipe/safe-html-pipe";
import {SplitPaneModule} from "ng2-split-pane/lib/ng2-split-pane";
import {FormsModule} from "@angular/forms";
import {ChartsModule} from 'ng2-charts';
import 'chartjs-plugin-datalabels';
import {MatSelectModule} from "@angular/material";
import {SharedDataTableModule} from "../../shared-modules/data-table/data-table.module";
import {ContentService} from "../../services/content-service.service";
import {AdminService} from "../../services/admin.service";
import {ConfigurationService} from "../../services/configuration.service";

@NgModule({
  imports: [
    CommonModule,
    ReportRoute,
    AccordionModule,
    ButtonModule,InputTextModule, CheckboxModule,
    TreeModule,
    TooltipModule,
    SharedHTMLPipeModule,
    SplitPaneModule, AutoCompleteModule,
    DropdownModule,CalendarModule,FormsModule,ChartsModule,SplitButtonModule,SharedDataTableModule,MatSelectModule,MenubarModule,DataTableModule
  ],
  declarations: [ReportComponent,ReportsRoleTreeComponent],

  providers: [
    ContentService, AdminService,
    ConfigurationService, {provide: APP_BASE_HREF, useValue: '/'}
  ]
})
export class ReportModule { }
