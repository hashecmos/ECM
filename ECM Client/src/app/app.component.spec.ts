import {TestBed, async} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {AppComponent} from './app.component';
import {AppMenuComponent, AppSubMenuComponent} from './components/generic-components/menu/app.menu.component';
import {AppTopBarComponent} from './components/generic-components/topbar/app.topbar.component';
import {AppFooterComponent} from './components/generic-components/footer/app.footer.component';
import {AppBreadcrumbComponent} from './components/generic-components/breadcrumb/app.breadcrumb.component';
// import { BreadcrumbService } from './breadcrumb.service';

describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      declarations: [
        AppComponent,
        AppMenuComponent,
        AppSubMenuComponent,
        AppTopBarComponent,
        AppFooterComponent,
        AppBreadcrumbComponent
      ],
      providers: []
    }).compileComponents();
  }));

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));
});
