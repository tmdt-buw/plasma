import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { MainModule } from './main/main.module';
import { Ng5SliderModule} from 'ng5-slider';

import { AppComponent } from './app.component';
import {RouterModule} from '@angular/router';
import {APP_ROUTES} from './app.routes';
import {HttpClientModule} from '@angular/common/http';
import { ApiModule } from 'build/openapi/api.module';
import {PlasmaModule} from './plasma/plasma.module';



@NgModule({
  bootstrap: [AppComponent],
  declarations: [
    AppComponent,

  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule.forRoot(APP_ROUTES),
    MainModule,
    HttpClientModule,
    Ng5SliderModule,
    ApiModule
  ],
  providers: [
  ]
})

export class AppModule { }
