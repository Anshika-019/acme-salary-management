const compactUsd = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  notation: 'compact',
  maximumFractionDigits: 1,
});

/** 612_400_000 -> "$612.4M": for headline totals where exact cents add noise. */
export function formatCompactUsd(value: number | null | undefined): string {
  return value === null || value === undefined ? '—' : compactUsd.format(value);
}

/** 257.1 -> "+257%", -45.2 -> "−45%" (true minus sign for legibility). */
export function formatDeviation(percent: number): string {
  const rounded = Math.round(Math.abs(percent));
  return percent >= 0 ? `+${rounded}%` : `\u2212${rounded}%`;
}
