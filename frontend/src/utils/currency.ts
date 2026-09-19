/** Formats a number as Indian Rupees, e.g. 166784.6 -> "₹1,66,785". */
export function formatRupees(value: number): string {
  return `₹${Math.round(value).toLocaleString('en-IN')}`;
}

/** Formats a number in any ISO 4217 currency, e.g. (1234.5, 'USD') ->
 * "$1,234.50" - used by the billing/payments pages, which support any
 * currency Razorpay does, unlike the clinic revenue pages above which are
 * always INR. */
export function formatCurrency(value: number, currencyCode: string): string {
  try {
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: currencyCode }).format(value);
  } catch {
    return `${currencyCode} ${value.toLocaleString()}`;
  }
}
