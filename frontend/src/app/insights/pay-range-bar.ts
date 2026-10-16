import { Component, computed, input } from '@angular/core';
import { Distribution, rangeGeometry } from './range-geometry';

const usd = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });

/**
 * One group's pay distribution on a shared scale: the thin line spans lowest to highest pay,
 * the box holds the middle half of employees, and the tick marks the median.
 */
@Component({
  selector: 'app-pay-range-bar',
  template: `
    <div class="track" role="img" [attr.aria-label]="label()">
      <span class="whisker" [style.left.%]="g().whiskerStart" [style.width.%]="g().whiskerWidth"></span>
      <span class="box" [style.left.%]="g().boxStart" [style.width.%]="g().boxWidth"></span>
      <span class="median" [style.left.%]="g().median"></span>
    </div>
  `,
  styles: `
    .track { position: relative; height: 22px; min-width: 220px; }
    .whisker { position: absolute; top: 10px; height: 2px; background: var(--rule); }
    .box { position: absolute; top: 4px; height: 14px; background: var(--ledger-soft); border: 1px solid var(--ledger); border-radius: 2px; box-sizing: border-box; }
    .median { position: absolute; top: 1px; width: 3px; height: 20px; margin-left: -1px; background: var(--ledger); border-radius: 1px; }
  `,
})
export class PayRangeBar {
  readonly distribution = input.required<Distribution>();
  readonly scaleMax = input.required<number>();

  protected readonly g = computed(() => rangeGeometry(this.distribution(), this.scaleMax()));
  protected readonly label = computed(() => {
    const d = this.distribution();
    return `Pay from ${usd.format(d.minUsd)} to ${usd.format(d.maxUsd)}; middle half ${usd.format(d.p25Usd)} to ${usd.format(d.p75Usd)}; median ${usd.format(d.medianUsd)}`;
  });
}
