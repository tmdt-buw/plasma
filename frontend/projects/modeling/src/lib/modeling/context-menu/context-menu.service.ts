import { Injectable, ViewContainerRef } from '@angular/core';
import { ConnectionPositionPair, Overlay, OverlayConfig, PositionStrategy } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { fromEvent, Observable, Subject, Subscription } from 'rxjs';
import { filter, take } from 'rxjs/operators';
import { ContextMenuEvent } from '../model/events/context-menu-event';


@Injectable({
  providedIn: 'root',
})
export class ContextMenuService {

  constructor(private overlay: Overlay) {
  }

  overlayRef: any;
  sub: Subscription;
  private afterClosed = new Subject<ContextMenuEvent>();
  onClosed = this.afterClosed.asObservable();

  private static getPositions(): ConnectionPositionPair[] {
    return [
      {
        originX: 'center',
        originY: 'bottom',
        overlayX: 'start',
        overlayY: 'top'
      },
      {
        originX: 'start',
        originY: 'bottom',
        overlayX: 'start',
        overlayY: 'top'
      },
      {
        originX: 'end',
        originY: 'bottom',
        overlayX: 'end',
        overlayY: 'top'
      }
    ];
  }

  open(origin: any, component: any, viewContainerRef: ViewContainerRef, data: any): Observable<any> {
    this.close(null);
    this.overlayRef = this.overlay.create(
      this.getOverlayConfig({origin})
    );

    const ref = this.overlayRef.attach(new ComponentPortal(component, viewContainerRef));
    Object.keys(data).forEach(key => {
      ref.instance[key] = data[key];
    });

    this.sub = fromEvent<MouseEvent>(document, 'click')
      .pipe(
        filter(event => {
          const clickTarget = event.target as HTMLElement;
          return (
            clickTarget !== origin &&
            (!!this.overlayRef &&
              !this.overlayRef.overlayElement.contains(clickTarget))
          );
        }),
        take(1)
      )
      .subscribe(() => {
        this.close(null);
      });
    return this.onClosed.pipe(take(1));
  }

  close = (data: any) => {
    if (this.sub) {
      this.sub.unsubscribe();
    }
    if (this.overlayRef) {
      this.overlayRef.dispose();
      this.overlayRef = null;
      this.afterClosed.next(data);
    }
  }

  private getOverlayPosition(origin: any): PositionStrategy {
    return this.overlay
      .position()
      .flexibleConnectedTo(origin)
      .withPositions(ContextMenuService.getPositions())
      .withPush(false);
  }

  private getOverlayConfig({origin}): OverlayConfig {
    return new OverlayConfig({
      hasBackdrop: false,
      backdropClass: 'popover-backdrop',
      positionStrategy: this.getOverlayPosition(origin),
      scrollStrategy: this.overlay.scrollStrategies.close()
    });
  }
}
