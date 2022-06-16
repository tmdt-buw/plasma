import { CommonModule, registerLocaleData } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import en from '@angular/common/locales/en';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { en_US, NZ_I18N } from 'ng-zorro-antd/i18n';
import { AppInitializerProvider } from './app-initializer.service';
import { AppComponent } from './app.component';
import { APP_ROUTES } from './app.routes';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzTypographyModule } from 'ng-zorro-antd/typography';

registerLocaleData(en);

@NgModule({
  bootstrap: [AppComponent],
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CommonModule,
    FlexLayoutModule,
    HttpClientModule,
    RouterModule.forRoot(APP_ROUTES),
    FormsModule,
    NzLayoutModule,
    NzTypographyModule
  ],
  providers: [
    AppInitializerProvider,
    {provide: NZ_I18N, useValue: en_US}]
})

export class AppModule {
}
