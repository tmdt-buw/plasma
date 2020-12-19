import { NgModule } from '@angular/core';
import { NavigationComponent } from './navigation/navigation.component';
import { SharedModule } from '../../shared-module';
import { RouterModule } from '@angular/router';
import { NAVIGATION_ROUTES } from './navigation.routes';

@NgModule({
  declarations: [NavigationComponent],
  imports: [
    SharedModule,
    RouterModule.forChild(NAVIGATION_ROUTES)
  ],
  exports: [NavigationComponent]
})
export class NavigationModule {
}
