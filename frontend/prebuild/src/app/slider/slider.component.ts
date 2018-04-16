import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { trigger, state, style, transition, animate, query, group } from '@angular/animations';

@Component({
  selector: 'app-slider',
  templateUrl: './slider.component.html',
  styleUrls: ['./slider.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('slide', [
      state('left', style({ transform: 'translateX(0)' })),
      state('center', style({ transform: 'translateX(-33.333%)' })),
      state('right', style({ transform: 'translateX(-66.666%)' })),
      transition('void => *', animate(0)),
      transition('* => *', animate(300))
    ]),
    trigger('fade', [
      state('active', style({ opacity: 1 })),
      state('inactive', style({ opacity: 0 })),
      transition('* => *', animate(300))
    ])
  ]
})
export class SliderComponent {
  @Input() activePane: PaneType = 'left';

  isActivePane(pane: PaneType) {
    console.log(`checking if ${pane} is active: ${this.activePane === pane}`);
    return this.activePane === pane ? 'active' : 'inactive';
  }
}

export type PaneType = 'left' | 'center' | 'right';
