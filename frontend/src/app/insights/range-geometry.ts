export interface Distribution {
  minUsd: number;
  p25Usd: number;
  medianUsd: number;
  p75Usd: number;
  maxUsd: number;
}

/** Positions (as % of the track width) for drawing one pay range on a shared scale. */
export interface RangeGeometry {
  whiskerStart: number;
  whiskerWidth: number;
  boxStart: number;
  boxWidth: number;
  median: number;
}

export function rangeGeometry(d: Distribution, scaleMax: number): RangeGeometry {
  if (scaleMax <= 0) {
    return { whiskerStart: 0, whiskerWidth: 0, boxStart: 0, boxWidth: 0, median: 0 };
  }
  const pct = (value: number) => Math.min(100, Math.max(0, (value / scaleMax) * 100));
  return {
    whiskerStart: pct(d.minUsd),
    whiskerWidth: pct(d.maxUsd) - pct(d.minUsd),
    boxStart: pct(d.p25Usd),
    boxWidth: pct(d.p75Usd) - pct(d.p25Usd),
    median: pct(d.medianUsd),
  };
}
