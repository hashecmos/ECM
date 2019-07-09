import {NgModule} from '@angular/core';
import {FilterCCPipe, FilterToPipe} from '../../pipes/recepients-filter.pipe';


@NgModule({
  declarations: [
    FilterToPipe,
    FilterCCPipe
  ],
  imports: [],
  providers: [],
  exports: [FilterToPipe,
    FilterCCPipe]
})
export class SharedRecipientsFilterModule {

}
