import { HttpErrorResponse } from '@angular/common/http';

export interface ApiError {
  message: string;
  /** Per-field messages from server-side validation, keyed by field name. */
  fieldErrors: Record<string, string>;
}

/**
 * Turns any failed request into a message the HR manager can act on.
 * The backend returns RFC 9457 problem details: { detail, errors? }.
 */
export function toApiError(error: unknown): ApiError {
  if (!(error instanceof HttpErrorResponse)) {
    return { message: 'Something unexpected happened. Try again.', fieldErrors: {} };
  }
  if (error.status === 0) {
    return { message: "Can't reach the server. Check your connection and try again.", fieldErrors: {} };
  }
  const body = error.error as { detail?: string; errors?: Record<string, string> } | null;
  return {
    message: body?.detail ?? `Request failed (${error.status}).`,
    fieldErrors: body?.errors ?? {},
  };
}
