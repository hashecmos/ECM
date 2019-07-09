import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {MainRoute} from './main.routes';
import {DashboardComponent} from './dashboard/dashboard.component';
import {MainComponent} from './main.component';
import {
  AccordionModule, ButtonModule, ChartModule, ContextMenuModule,
  PanelModule, SelectButtonModule,
  TooltipModule, TreeModule, TabViewModule, DialogModule
} from 'primeng/primeng';
import {HttpModule} from '@angular/http';
import {AsideModule} from 'ng2-aside';
import {SharedDataTableModule} from '../../shared-modules/data-table/data-table.module';
import {RecentsComponent} from '../../components/shortcut-components/recents/recents.component';
import {TeamsharedDocsComponent} from '../../components/shortcut-components/teamshared-docs/teamshared-docs.component';
import {FavouritesComponent} from '../../components/shortcut-components/favourites/favourites.component';
import {AdminService} from '../../services/admin.service';
import {ConfigurationService} from '../../services/configuration.service';
import {NewsService} from '../../services/news.service';
import {WorkflowService} from '../../services/workflow.service';
import {ContentService} from '../../services/content-service.service';
import {SharedRightPanelModule} from '../../shared-modules/right-panel/right-panel.module';
import {SharedTreeModule} from '../../shared-modules/tree-module/tree.module';
import {BusyModule} from 'angular2-busy';
import {
  MatTabsModule
} from '@angular/material';
import {SharedHTMLPipeModule} from '../../shared-modules/safe-html-pipe/safe-html-pipe';
import {ChartsModule} from 'ng2-charts';
//import 'chart.piecelabel.js';
import 'chartjs-plugin-datalabels';
import {CoreService} from "../../services/core.service";
@NgModule({
  imports: [CommonModule,
    FormsModule,
    MainRoute, ReactiveFormsModule,
    SharedDataTableModule,
    MatTabsModule,
    HttpModule,
    AccordionModule,
    ButtonModule,
    ChartModule,
    PanelModule,
    SelectButtonModule,
    TooltipModule,
    TreeModule, TabViewModule,DialogModule,
    SharedRightPanelModule, SharedTreeModule, BusyModule,SharedHTMLPipeModule,
    AsideModule, ChartsModule,
    ContextMenuModule],
  declarations: [
    DashboardComponent,
    MainComponent,
    RecentsComponent, TeamsharedDocsComponent, FavouritesComponent
  ],
  providers: [
    ContentService,
    WorkflowService, NewsService, AdminService, ConfigurationService,
  ]
})
export class MainModule {
}
