import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Http, HttpModule, RequestOptions, XHRBackend} from '@angular/http';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {LocationStrategy, HashLocationStrategy} from '@angular/common';
import {AppRoutes} from './app.routes';
import 'rxjs/add/operator/toPromise';

import {
  GrowlModule, ConfirmationService, ProgressSpinnerModule, ConfirmDialogModule,
  TreeModule
} from 'primeng/primeng';
import {ButtonModule} from 'primeng/primeng';
import {SharedModule} from 'primeng/primeng';
import {InputTextModule} from 'primeng/primeng';

import {AppComponent} from './app.component';
import {AppMenuComponent, AppSubMenuComponent} from './components/generic-components/menu/app.menu.component';
import {AppBreadcrumbComponent} from './components/generic-components/breadcrumb/app.breadcrumb.component';
import {AppTopBarComponent} from './components/generic-components/topbar/app.topbar.component';
import {AppFooterComponent} from './components/generic-components/footer/app.footer.component';
import {UserService} from './services/user.service';
import {HeaderComponent} from './components/generic-components/header/header.component';
import {HomeComponent} from './components/generic-components/home/home.component';
import {AuthGuardHomeService} from './services/auth-guard-home.service';
import {SharedDocumentCartModule} from './shared-modules/document-cart/document-cart.module';
import {DocumentService} from './services/document.service';
import {GrowlService} from './services/growl.service';
import {CoreService} from './services/core.service';
import {BrowserEvents} from './services/browser-events.service';
import {BreadcrumbService} from './services/breadcrumb.service';
import {BusyModule} from "angular2-busy";
import {DefaultUrlGuardService} from "./services/default-url-guard.service";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {HttpInterceptorService} from "./services/http-interceptor.service";
import {WorkflowService} from "./services/workflow.service";
import {ReportService} from "./services/report.service";
@NgModule({
  imports: [
    BrowserModule,
    FormsModule, ReactiveFormsModule,
    AppRoutes,
    HttpModule,
    BrowserAnimationsModule,
    GrowlModule,
    ButtonModule,
    SharedModule,
    InputTextModule,
    SharedDocumentCartModule,
    ProgressSpinnerModule,
    ConfirmDialogModule,
    BusyModule,
    HttpClientModule
  ],
  declarations: [
    AppComponent,
    HomeComponent,
    AppMenuComponent,
    AppSubMenuComponent,
    AppBreadcrumbComponent,
    AppTopBarComponent,
    AppFooterComponent,
    HeaderComponent,
  ],
  providers: [
    {provide: LocationStrategy, useClass: HashLocationStrategy}, UserService,
    AuthGuardHomeService, DefaultUrlGuardService, DocumentService, GrowlService, ConfirmationService,
    CoreService, BrowserEvents, BreadcrumbService,WorkflowService, ReportService,
    HttpClient,{
    provide: HTTP_INTERCEPTORS,
    useClass: HttpInterceptorService,
    multi: true,
  }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
