/** Formats a number as Indian Rupees, e.g. 166784.6 -> "₹1,66,785". */
export function formatRupees(value: number): string {
  return `₹${Math.round(value).toLocaleString('en-IN')}`;
}
