import { DragDropModule } from '@angular/cdk/drag-drop';
import { FullscreenOverlayContainer, OverlayContainer, OverlayModule } from '@angular/cdk/overlay';
import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { ApiModule, BASE_PATH } from './api/generated/dms';
import { ApiModule as KGSApiModule, BASE_PATH as KGS_BASE_PATH } from './api/generated/kgs';
import { PlsSemanticClassMenuComponent } from './modeling/context-menu/semantic-class-menu/semantic-class-menu.component';
import { PlsRelationConceptMenuComponent } from './modeling/context-menu/relation-menu/relation-menu.component';
import { PlsDiagramComponent } from './modeling/diagram/diagram.component';
import { InlineInputEditComponent } from './modeling/dialogs/common/inline-edit/inline-edit.component';
import { PlsConceptDialogComponent } from './modeling/dialogs/concept-dialog/concept-dialog.component';
import { PlsImportDialogComponent } from './modeling/dialogs/import-dialog/import-dialog.component';
import { PlsEditEntityDialogComponent } from './modeling/dialogs/edit-entity-dialog/edit-entity-dialog.component';
import { PlsEditRelationDialogComponent } from './modeling/dialogs/edit-relation-dialog/edit-relation-dialog.component';
import { PlsSemanticNodeDetailsComponent } from './modeling/dialogs/semantic-node-dialog/semantic-node.component';
import { PlsSyntaxNodeDetailsComponent } from './modeling/dialogs/syntax-node-dialog/syntax-node.component';
import { PlsFullscreenDirective } from './modeling/directives/fullscreen.directive';
import { PlsModelingComponent } from './modeling/modeling.component';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { PlsNamedEntityMenuComponent } from './modeling/context-menu/named-entity-menu/named-entity-menu.component';
import { PlsLiteralMenuComponent } from './modeling/context-menu/literal-menu/literal-menu.component';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { UploadOntologyComponent } from './modeling/dialogs/upload-ontology/upload-ontology.component';
import { NzResultModule } from 'ng-zorro-antd/result';
import { CopySemanticModelComponent } from './modeling/dialogs/copy-semantic-model/copy-semantic-model.component';
import { NzNotificationModule } from 'ng-zorro-antd/notification';
import { ManageOntologiesDialogComponent } from './modeling/dialogs/manage-ontologies-dialog/manage-ontologies-dialog.component';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { UriGeneratorComponent } from './modeling/dialogs/common/uri-generator/uri-generator.component';


@NgModule({
  declarations: [
    PlsModelingComponent, PlsDiagramComponent, PlsSyntaxNodeDetailsComponent,
    PlsSemanticNodeDetailsComponent, PlsConceptDialogComponent, PlsEditEntityDialogComponent,
    PlsEditRelationDialogComponent, PlsImportDialogComponent,
    InlineInputEditComponent, PlsSemanticClassMenuComponent, PlsRelationConceptMenuComponent, PlsFullscreenDirective,
    PlsNamedEntityMenuComponent, PlsLiteralMenuComponent, UploadOntologyComponent, CopySemanticModelComponent, ManageOntologiesDialogComponent, UriGeneratorComponent
  ],
  imports: [
    // Plasma
    ApiModule,
    KGSApiModule,
    // Angular
    CommonModule,
    DragDropModule,
    FlexLayoutModule,
    FormsModule,
    ReactiveFormsModule,
    OverlayModule,
    NzButtonModule,
    NzIconModule,
    NzToolTipModule,
    NzSwitchModule,
    NzFormModule,
    NzInputModule,
    NzDropDownModule,
    NzSelectModule,
    NzSpinModule,
    NzCardModule,
    NzDividerModule,
    NzTypographyModule,
    NzUploadModule,
    NzModalModule,
    NzTabsModule,
    NzAlertModule,
    NzCheckboxModule,
    NzRadioModule,
    NzTagModule,
    NzCollapseModule,
    NzResultModule,
    NzNotificationModule,
    NzListModule,
    NzPopconfirmModule,
    NzDescriptionsModule
  ],
  exports: [PlsModelingComponent]
})
export class PlsModelingModule {
  public static setBasePath(basePath: string): ModuleWithProviders<PlsModelingModule> {
    return {
      ngModule: PlsModelingModule,
      providers: [
        {provide: BASE_PATH, useValue: basePath},
        {provide: KGS_BASE_PATH, useValue: basePath},
        // enable correct display of cdk overlay elements like dialogs in fullscreen mode
        {provide: OverlayContainer, useClass: FullscreenOverlayContainer}
      ]
    };
  }
}
