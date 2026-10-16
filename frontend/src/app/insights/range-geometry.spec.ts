import { rangeGeometry } from './range-geometry';

describe('rangeGeometry', () => {
  const distribution = { minUsd: 20_000, p25Usd: 40_000, medianUsd: 50_000, p75Usd: 60_000, maxUsd: 100_000 };

  it('places every marker on a shared scale', () => {
    expect(rangeGeometry(distribution, 200_000)).toEqual({
      whiskerStart: 10,
      whiskerWidth: 40,
      boxStart: 20,
      boxWidth: 10,
      median: 25,
    });
  });

  it('clamps values beyond the scale', () => {
    expect(rangeGeometry(distribution, 50_000).whiskerStart + rangeGeometry(distribution, 50_000).whiskerWidth).toBe(100);
  });

  it('draws nothing when there is no scale', () => {
    expect(rangeGeometry(distribution, 0).boxWidth).toBe(0);
  });
});
