/**
 * Descarga una tabla como CSV que Excel abre bien: UTF-8 con BOM (acentos) y
 * celdas entre comillas cuando traen coma, comillas o saltos de línea.
 */
export function downloadCsv(filename: string, head: string[], rows: unknown[][]): void {
  const lines = [head, ...rows].map((r) => r.map(csvCell).join(','));
  const csv = '﻿' + lines.join('\r\n');
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

function csvCell(value: unknown): string {
  if (value == null) {
    return '';
  }
  const text = String(value);
  return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
}
