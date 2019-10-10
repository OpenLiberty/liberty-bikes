import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { trigger, state, style, transition, animate, query, group, animateChild } from '@angular/animations';

@Component({
  selector: 'app-slider',
  templateUrl: './slider.component.html',
  styleUrls: ['./slider.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('slide', [
      state('left', style({ transform: 'translateX(0)' })),
      state('center', style({ transform: 'translateX(-20%)' })),
      state('ai', style({ transform: 'translateX(-40%)'})),
      state('right', style({ transform: 'translateX(-60%)' })),
      state('queue', style({ transform: 'translateX(-80%)'})),

      transition('void => *', animate(0)),
      transition('* => *', [
        group([
          query('@fade', animateChild()),
          animate('300ms ease-out')
        ])
      ])
    ]),
    trigger('fade', [
      state('active', style({ visibility: 'visible', opacity: 1 })),
      state('inactive', style({ visibility: 'hidden', opacity: 0 })),
      transition('* => *', animate('200ms'))
    ])
  ]
})
export class SliderComponent {
  @Input() activePane: PaneType = 'left';

  isActivePane(pane: PaneType) {
    return this.activePane === pane ? 'active' : 'inactive';
  }
}

export type PaneType = 'left' | 'center' | 'right' | 'queue' | 'ai';
