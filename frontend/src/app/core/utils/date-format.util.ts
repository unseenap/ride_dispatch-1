/**
 * Shared date formatting helper used across trip list/detail views.
 */
export function formatTripTimestamp(value: string | null | undefined): string {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  return date.toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  });
}
