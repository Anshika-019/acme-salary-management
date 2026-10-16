import { HttpErrorResponse } from '@angular/common/http';
import { toApiError } from './api-error';

describe('toApiError', () => {
  it('uses the problem detail message and field errors from the API', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { detail: 'One or more fields are invalid.', errors: { email: 'must be a well-formed email address' } },
    });

    expect(toApiError(error)).toEqual({
      message: 'One or more fields are invalid.',
      fieldErrors: { email: 'must be a well-formed email address' },
    });
  });

  it('explains when the server cannot be reached', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(toApiError(error).message).toContain("Can't reach the server");
  });

  it('falls back to the status code when the body has no detail', () => {
    const error = new HttpErrorResponse({ status: 502, error: null });

    expect(toApiError(error).message).toBe('Request failed (502).');
  });
});
