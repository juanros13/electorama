"""
Escribe en E4 los totales OFICIALES de las elecciones locales, tal como los
publica el IEPCT en su "concentrado" (ayu_/dip_/gub_resumen_general.xlsx):
una fila por municipio (ayuntamientos) o distrito local (diputaciones y
gubernatura), más la fila de TOTAL del estado.

Como las etiquetas del resumen ("PAN, PRD Y MC") no se escriben igual que las
del archivo por casilla ("PAN_PRD_MC"), cada columna se compara por el
CONJUNTO de partidos que suma:

  SET:MC,PAN,PRD        la suma de las opciones con exactamente esos partidos
  KIND:INDEPENDENT      todas las candidaturas independientes juntas
  TOTAL, NULL, NON_REGISTERED, NOMINAL_LIST

Las columnas "TOTAL <coalición>", los votos válidos y la participación se
omiten: son derivadas. Una celda vacía = la opción no estaba en esa boleta.

Uso (desde C:\\Users\\juanr\\geo-2026\\electoral-history\\fuentes):
  python iepct_official.py <ruta a E4__official_totals.sql>
"""

import re
import sys

from iepct_catalog import municipality, parties, plain  # mismas reglas que el catálogo

FILES = [
    (2018, 'GOVERNOR', 'iepct/2018/gub_resumen_general.xlsx'),
    (2024, 'GOVERNOR', 'iepct/2024/gub_resumen_general.xlsx'),
    (2018, 'LOCAL_DEPUTY', 'iepct/2018/dip_resumen_general.xlsx'),
    (2021, 'LOCAL_DEPUTY', 'iepct/2021/dip_resumen_general.xlsx'),
    (2024, 'LOCAL_DEPUTY', 'iepct/2024/dip_resumen_general.xlsx'),
    (2018, 'MAYOR', 'iepct/2018/ayu_resumen_general.xlsx'),
    (2021, 'MAYOR', 'iepct/2021/ayu_resumen_general.xlsx'),
    (2024, 'MAYOR', 'iepct/2024/ayu_resumen_general.xlsx'),
]

SKIP = ('SECCIONES', 'CASILLAS', 'ACTAS', 'TOTAL ', 'NUMERO DE VOTOS VALIDOS', 'PARTICIPACION')


def code_of(header: str, year: int):
    h = plain(' '.join(header.split()))
    if h.startswith(SKIP) or h in ('MUNICIPIO', 'DISTRITO'):
        return None
    if 'NO REGISTRAD' in h or h.startswith('NUMERO DE VOTOS CANDIDATURAS'):
        return 'NON_REGISTERED'
    if h.startswith('NUMERO DE VOTOS NULOS'):
        return 'NULL'
    if h.startswith('TOTAL DE VOTOS'):
        return 'TOTAL'
    if h.startswith('LISTA NOMINAL'):
        return 'NOMINAL_LIST'
    if 'INDEPENDIENTE' in h:
        return 'KIND:INDEPENDENT'
    return 'SET:' + ','.join(sorted(parties(h, year)))


def main():
    from openpyxl import load_workbook

    e4_path = sys.argv[1]
    rows = []
    for year, office, path in FILES:
        ws = load_workbook(path, read_only=True, data_only=True).worksheets[0]
        data = list(ws.iter_rows(values_only=True))
        at = next(i for i, r in enumerate(data) if r and str(r[0]).strip() in ('Municipio', 'Distrito'))
        header = [str(h) if h is not None else '' for h in data[at]]
        codes = [code_of(h, year) if h else None for h in header]
        rows.append(f'  -- {office} {year}: {path}')
        for r in data[at + 1:]:
            if not r or r[0] is None:
                break
            name = ' '.join(str(r[0]).split())
            if plain(name).startswith('TOTAL'):
                mun, district = 'NULL', 'NULL'
            elif office == 'MAYOR':
                mun, district = str(municipality(name)), 'NULL'
            else:
                mun, district = 'NULL', f"'{int(re.search(r'\d+', name).group())}'"
            for c, value in zip(codes, r):
                if c is None or value is None or str(value).strip() == '':
                    continue
                rows.append(f"  ({year}, '{office}', {mun}, {district}, '{c}', {int(float(value))}),")
            if mun == 'NULL' and district == 'NULL':
                break

    body = '\n'.join(rows)
    body = body[: body.rfind(',')] + ';'
    block = (
        '-- >>> iepct_official.py  (generado: no editar a mano, correr tools/normalize/iepct_official.py)\n'
        'INSERT INTO stg_official (year, office, municipality_cve, district, code, value) VALUES\n'
        + body + '\n-- <<< iepct_official.py'
    )
    text = open(e4_path, encoding='utf-8').read()
    start = text.index('-- >>> iepct_official.py')
    end = text.index('-- <<< iepct_official.py') + len('-- <<< iepct_official.py')
    open(e4_path, 'w', encoding='utf-8', newline='\n').write(text[:start] + block + text[end:])
    print(f'{len(rows)} filas')


if __name__ == '__main__':
    main()
