"""Extrae la lista nominal por sección de los 32 estados del Excel de la DERFE.

La fuente es la "Estadística de Padrón Electoral y Lista Nominal del Electorado"
por sección (datos abiertos del INE), p.ej.
DatosAbiertos-derfe-pdln_edms_sexo_20251031_Nal_Ext.xlsx (corte 31-oct-2025).
Se toma "LISTA NOMINAL DE ELECTORES NACIONAL — TOTAL"; la fila con sección 0 de
cada estado es la de residentes en el extranjero y se descarta.

    python ln-nacional.py <xlsx> <salida.txt>

Salida: entidad|distrito_f|municipio|seccion|ln, que carga ln-nacional.sql.
"""
import sys

import openpyxl

COL_ENT, COL_DF, COL_MUN, COL_SECC, COL_LN = 0, 1, 2, 3, 15


def main(src, dst):
    ws = openpyxl.load_workbook(src, read_only=True).worksheets[0]
    rows = 0
    with open(dst, 'w', encoding='utf-8', newline='\n') as out:
        for r in ws.iter_rows(min_row=3, values_only=True):
            if not isinstance(r[COL_SECC], int) or r[COL_SECC] == 0:
                continue
            out.write('|'.join(str(r[c]) for c in (COL_ENT, COL_DF, COL_MUN, COL_SECC, COL_LN)) + '\n')
            rows += 1
    print(f'{rows} secciones -> {dst}')


if __name__ == '__main__':
    main(*sys.argv[1:3])
