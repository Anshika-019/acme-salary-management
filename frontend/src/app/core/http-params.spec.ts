import { toHttpParams } from './http-params';

describe('toHttpParams', () => {
  it('keeps set values and drops empty ones', () => {
    const params = toHttpParams({ search: 'asha', country: '', department: undefined, jobTitle: null, page: 0 });

    expect(params.keys()).toEqual(['search', 'page']);
    expect(params.get('page')).toBe('0');
  });
});
