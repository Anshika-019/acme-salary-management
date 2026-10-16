import { formatCompactUsd, formatDeviation } from './format';

describe('formatCompactUsd', () => {
  it('abbreviates large totals', () => {
    expect(formatCompactUsd(612_400_000)).toBe('$612.4M');
  });

  it('shows a dash for missing values', () => {
    expect(formatCompactUsd(null)).toBe('—');
  });
});

describe('formatDeviation', () => {
  it('signs and rounds the percentage', () => {
    expect(formatDeviation(257.1)).toBe('+257%');
    expect(formatDeviation(-45.4)).toBe('\u221245%');
  });
});
