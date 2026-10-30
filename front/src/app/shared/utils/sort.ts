/** Ordena un array por un atributo (asc por defecto). Mutación in-place como el viejo. */
export function sortData<T>(
  array: T[],
  attribute: keyof T,
  ascending = true
): T[] {
  return array.sort((a, b) => {
    if (a[attribute] < b[attribute]) {
      return ascending ? -1 : 1;
    }
    if (a[attribute] > b[attribute]) {
      return ascending ? 1 : -1;
    }
    return 0;
  });
}
