"""
Normalizador de resultados electorales oficiales → formato largo para psql.

Sólo cambia la forma: lee un archivo por casilla (CSV del INE o XLSX del IEPCT),
se queda con Tabasco y escribe dos archivos separados por '|':

  st_<año>_<cargo>.txt  una fila por casilla
      station_key|section|district|municipality|station_type|nominal_list|
      total_votes|null_votes|non_registered_votes|counted|source_row
  vt_<año>_<cargo>.txt  una fila por casilla y columna de votos de la boleta
      station_key|raw_label|votes

No decide qué es un partido, una coalición o un total derivado: eso lo manda
`ballot_option` en E2, y E3 aborta si llega una etiqueta que no está ahí.
Por eso aquí salen TODAS las columnas de votos tal cual vienen.

Uso (desde la carpeta de las fuentes):
  python normalize.py <año> <cargo> <salida>
  python normalize.py 2024 PRESIDENT C:\\Users\\juanr\\geo-2026\\electoral-history
  python normalize.py sections <año> <salida>    sección → municipio de ese año

Sólo usa la biblioteca estándar + openpyxl (que ya está instalado).
"""

import csv
import io
import re
import sys
import zipfile
from pathlib import Path

STATE_ID = '27'

# --- fuentes -----------------------------------------------------------------
# Cada fuente dice dónde está el archivo y qué columnas son fijas; el resto de
# columnas entre `first_vote` y `last_vote` (inclusive) son votos de la boleta.
INE = {
    (2024, 'PRESIDENT'): ('ine/20240608_2030_COMPUTOS.zip', '20240608_2030_COMPUTOS_PRES.zip', 'PRES_2024.csv'),
    (2024, 'SENATOR'): ('ine/20240608_2030_COMPUTOS.zip', '20240608_2030_COMPUTOS_SEN.zip', 'SEN_2024.csv'),
    (2024, 'FEDERAL_DEPUTY'): ('ine/20240608_2030_COMPUTOS.zip', '20240608_2030_COMPUTOS_DIP_FED.zip', 'DIP_FED_2024.csv'),
    (2021, 'FEDERAL_DEPUTY'): ('ine/20210611_1000_CW.zip', '20210611_1000_CW_diputaciones.zip', '20210611_1000_CW_diputaciones/diputaciones.csv'),
    (2018, 'PRESIDENT'): ('ine/20180708_2130_CW.zip', '20180708_2130_CW_presidencia.zip', '20180708_2130_CW_presidencia/presidencia.csv'),
    (2018, 'SENATOR'): ('ine/20180708_2130_CW.zip', '20180708_2130_CW_senadurias.zip', '20180708_2130_CW_senadurias/senadurias.csv'),
    (2018, 'FEDERAL_DEPUTY'): ('ine/20180708_2130_CW.zip', '20180708_2130_CW_diputaciones.zip', '20180708_2130_CW_diputaciones/diputaciones.csv'),
}

# Nombres de columna que cambian entre años en los CSV del INE.
INE_STATE = ('ID_ENTIDAD', 'ID_ESTADO')
INE_DISTRICT = ('ID_DISTRITO_FEDERAL', 'ID_DISTRITO')
INE_NOMINAL = ('LISTA_NOMINAL', 'LISTA_NOMINAL_CASILLA')
INE_NULL = ('VOTOS NULOS', 'VN')
INE_NON_REGISTERED = ('CANDIDATO/A NO REGISTRADO/A', 'CNR')
# la última columna de votos de partido va justo antes de no registrados
INE_FIRST_VOTE = 'PAN'


def clean(value: str) -> str:
    """El INE escribe ="01" o '270001B0100' para que Excel no quite ceros."""
    return value.strip().replace('="', '').replace('"', '').replace("'", '')


def to_int(value) -> int:
    if value is None:
        return 0
    text = str(value).strip().replace(',', '')
    if text in ('', '-', 'N/A'):
        return 0
    return int(float(text))


def first(header: dict, names) -> int:
    for name in names:
        if name in header:
            return header[name]
    raise KeyError(f'ninguna de {names} está en el encabezado')


def read_ine(year: int, office: str, root: Path):
    outer, inner, member = INE[(year, office)]
    with zipfile.ZipFile(root / outer) as zf:
        with zipfile.ZipFile(io.BytesIO(zf.read(inner))) as iz:
            text = iz.read(member).decode('latin-1')

    lines = text.splitlines()
    header_at = next(i for i, line in enumerate(lines) if line.startswith('CLAVE_CASILLA'))
    names = lines[header_at].split('|')
    header = {name: i for i, name in enumerate(names)}

    state = first(header, INE_STATE)
    district = first(header, INE_DISTRICT)
    nominal = first(header, INE_NOMINAL)
    null = first(header, INE_NULL)
    non_registered = first(header, INE_NON_REGISTERED)
    act = first(header, ('TIPO_ACTA', 'NUM_ACTA_IMPRESO'))
    vote_columns = range(header[INE_FIRST_VOTE], non_registered)

    rows = [
        (n, line.split('|'))
        for n, line in enumerate(lines[header_at + 1:], start=header_at + 2)
        if line and clean(line.split('|')[state]) == STATE_ID
    ]

    # En senado y diputaciones, cada casilla especial levanta DOS actas: la de
    # mayoría relativa y la de representación proporcional (el voto de quien está
    # fuera de su distrito). Aquí se analiza mayoría relativa: se queda la MR.
    # 2024 la marca en TIPO_ACTA ("3EMR"/"3ERP"); antes, la MR es la de número
    # menor (diputaciones 2018: 7 MR / 8 RP, cuadrado contra el total MR oficial).
    # Sólo las especiales (S): el voto en prisión de 2021 trae 6 actas con la
    # misma clave de casilla, una por distrito, y todas son de mayoría relativa.
    by_station = {}
    for n, row in rows:
        key = clean(row[header['CLAVE_CASILLA']])
        if row[header['TIPO_CASILLA']].strip() != 'S':
            key = clean(row[header['CLAVE_ACTA']])
        by_station.setdefault(key, []).append((n, row))
    kept, dropped = [], 0
    for station, actas in by_station.items():
        if len(actas) == 1:
            kept.append(actas[0])
            continue
        mr = [a for a in actas if a[1][act].strip().upper().endswith('MR')]
        if not mr:
            numbers = [clean(a[1][act]) for a in actas]
            if not all(x.isdigit() for x in numbers):
                raise SystemExit(f'{station}: no sé cuál acta es la MR entre {numbers}')
            mr = [min(actas, key=lambda a: int(clean(a[1][act])))]
        kept.append(mr[0])
        dropped += len(actas) - 1
    if dropped:
        print(f'  {dropped} actas de representación proporcional de casillas especiales, fuera')

    for row_number, row in sorted(kept, key=lambda a: a[0]):
        # la clave del ACTA es única; la de casilla se repite (prisión 2021)
        station_key = clean(row[header['CLAVE_ACTA']])
        votes = {names[c]: to_int(row[c]) for c in vote_columns}
        null_votes = to_int(row[null])
        nr_votes = to_int(row[non_registered])
        yield {
            'station_key': station_key,
            'section': int(clean(row[header['SECCION']])),
            'district': clean(row[district]).lstrip('0') or '0',
            'municipality': '',  # el INE no trae municipio: sale de la sección en E3
            'station_type': row[header['TIPO_CASILLA']].strip(),
            'nominal_list': to_int(row[nominal]),
            # total = suma de la boleta, como TOTAL_VOTOS_CALCULADOS
            'total_votes': sum(votes.values()) + null_votes + nr_votes,
            'null_votes': null_votes,
            'non_registered_votes': nr_votes,
            'counted': 't',
            'source_row': row_number,
            'votes': votes,
        }


# --- IEPCT ---------------------------------------------------------------------
IEPCT = {
    'MAYOR': 'ayu_resumen_general_casillas.xlsx',
    'LOCAL_DEPUTY': 'dip_resumen_general_casillas_mr.xlsx',
    'GOVERNOR': 'gub_resumen_general_casillas.xlsx',
}

# primera columna que ya no es de votos de la boleta
IEPCT_SUMMARY = ('NÚMERO DE VOTOS', 'TOTAL DE VOTOS')


def station_type(casilla: str) -> str:
    """BÁSICA → B, CONTIGUA 01 → C, EXTRAORDINARIA → E, ESPECIAL → S, VOTO ANTICIPADO → A."""
    word = casilla.split()[0].upper()
    for prefix, code in (('B', 'B'), ('CONT', 'C'), ('EXTRA', 'E'), ('ESP', 'S'), ('VOTO', 'A')):
        if word.startswith(prefix):
            return code
    raise SystemExit(f'tipo de casilla desconocido: {casilla!r}')


def iepct_header(ws):
    rows = list(ws.iter_rows(values_only=True))
    at = next(i for i, r in enumerate(rows) if r and r[0] == 'DISTRITO ELECTORAL')
    names = [' '.join(str(h).split()) if h is not None else '' for h in rows[at]]
    return rows, at, names


def read_iepct(year: int, office: str, root: Path):
    from openpyxl import load_workbook

    ws = load_workbook(root / 'iepct' / str(year) / IEPCT[office], read_only=True, data_only=True).worksheets[0]
    rows, at, names = iepct_header(ws)
    col = {n.upper(): i for i, n in enumerate(names)}

    def find(*starts):
        for name, i in col.items():
            if any(name.startswith(s) for s in starts):
                return i
        raise SystemExit(f'{year} {office}: no encuentro {starts}')

    first_vote = names.index('CASILLA') + 1
    last_vote = min(i for i, n in enumerate(names) if n.upper().startswith(IEPCT_SUMMARY))
    vote_columns = range(first_vote, last_vote)
    nr = find('NÚMERO DE VOTOS CANDIDAT')
    null = find('NÚMERO DE VOTOS NULOS')
    total = find('TOTAL DE VOTOS')
    nominal = find('LISTA NOMINAL')
    annulled = col.get('ANULADA')

    for row_number, r in enumerate(rows[at + 1:], start=at + 2):
        if not r or not r[4] or r[3] in (None, ''):
            continue
        casilla = ' '.join(str(r[4]).split())
        section = int(str(r[3]).strip())
        district = int(re.search(r'\d+', str(r[0])).group())
        municipality = ' '.join(str(r[1]).split())
        # celda vacía = la opción no estaba en la boleta de ese territorio: no se escribe
        votes = {names[c]: to_int(r[c]) for c in vote_columns if r[c] is not None and str(r[c]).strip() != ''}
        yield {
            # el archivo va separado por '|': la clave usa '/'
            'station_key': f'{district}/{municipality}/{section}/{casilla}',
            'section': section,
            'district': str(district),
            'municipality': municipality,
            'station_type': station_type(casilla),
            'nominal_list': to_int(r[nominal]),
            'total_votes': to_int(r[total]),
            'null_votes': to_int(r[null]),
            'non_registered_votes': to_int(r[nr]),
            'counted': 'f' if annulled is not None and str(r[annulled]).strip().upper() == 'SI' else 't',
            'source_row': row_number,
            'votes': votes,
        }


def write(rows, year: int, office: str, out: Path):
    out.mkdir(parents=True, exist_ok=True)
    st_path = out / f'st_{year}_{office}.txt'
    vt_path = out / f'vt_{year}_{office}.txt'
    stations = 0
    with open(st_path, 'w', encoding='utf-8', newline='\n') as st, open(
        vt_path, 'w', encoding='utf-8', newline='\n'
    ) as vt:
        sw = csv.writer(st, delimiter='|', lineterminator='\n')
        vw = csv.writer(vt, delimiter='|', lineterminator='\n')
        for r in rows:
            stations += 1
            sw.writerow(
                [r['station_key'], r['section'], r['district'], r['municipality'], r['station_type'],
                 r['nominal_list'], r['total_votes'], r['null_votes'], r['non_registered_votes'],
                 r['counted'], r['source_row']]
            )
            for label, votes in r['votes'].items():
                vw.writerow([r['station_key'], label.strip(), votes])

    # psql no expande variables en \copy: E3 incluye este archivo con \i, que sí
    # las acepta. Así E3 sabe qué archivos leer sin copiarlos a un nombre fijo.
    load_path = out / f'load_{year}_{office}.sql'
    with open(load_path, 'w', encoding='utf-8', newline='\n') as load:
        load.write(f"\\copy stg_station from '{st_path.name}' with (format csv, delimiter '|')\n")
        load.write(f"\\copy stg_vote from '{vt_path.name}' with (format csv, delimiter '|')\n")
    print(f'{st_path.name}: {stations} casillas')


# De qué archivo del INE sale el distrito FEDERAL de cada sección, por año.
FEDERAL_DISTRICTS = {2018: (2018, 'FEDERAL_DEPUTY'), 2021: (2021, 'FEDERAL_DEPUTY'),
                     2024: (2024, 'FEDERAL_DEPUTY')}


def write_sections(year: int, out: Path):
    """
    sections_<año>.txt: sección|municipio|distrito local|distrito federal.

    Municipio y distrito LOCAL salen de los archivos de diputaciones locales del
    IEPCT de ese año (el INE no trae municipio y geography ya no conserva todas
    las secciones de 2018 y 2021); el distrito FEDERAL, del CSV de diputaciones
    federales del INE del mismo año. Así cada elección se puede leer por
    municipio, distrito local o distrito federal en el marco de SU año.
    """
    from openpyxl import load_workbook

    ws = load_workbook(Path.cwd() / 'iepct' / str(year) / IEPCT['LOCAL_DEPUTY'],
                       read_only=True, data_only=True).worksheets[0]
    rows, at, _ = iepct_header(ws)
    sections = {}
    for r in rows[at + 1:]:
        try:
            section = int(str(r[3]).strip())
        except (TypeError, ValueError):
            continue
        if section == 0:  # voto anticipado: no es una sección real
            continue
        local = int(re.search(r'\d+', str(r[0])).group())
        sections.setdefault(section, set()).add((' '.join(str(r[1]).split()), local))

    federal = {}
    for station in read_ine(*FEDERAL_DISTRICTS[year], Path.cwd()):
        if station['district'].isdigit() and station['section'] > 0:
            federal.setdefault(station['section'], set()).add(int(station['district']))

    conflicts = {s: v for s, v in sections.items() if len(v) > 1}
    conflicts.update({s: v for s, v in federal.items() if len(v) > 1})
    if conflicts:
        raise SystemExit(f'secciones en más de un municipio o distrito: {conflicts}')
    path = out / f'sections_{year}.txt'
    missing = 0
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        for section in sorted(sections):
            municipality, local = next(iter(sections[section]))
            fed = next(iter(federal.get(section, {''})))
            missing += fed == ''
            f.write(f'{section}|{municipality}|{local}|{fed}\n')
    print(f'{path.name}: {len(sections)} secciones, {missing} sin distrito federal')


def main():
    if sys.argv[1] == 'sections':
        write_sections(int(sys.argv[2]), Path(sys.argv[3]))
        return
    year, office, out = int(sys.argv[1]), sys.argv[2], Path(sys.argv[3])
    root = Path.cwd()
    if (year, office) in INE:
        rows = read_ine(year, office, root)
    elif office in IEPCT:
        rows = read_iepct(year, office, root)
    else:
        raise SystemExit(f'sin fuente configurada para {year} {office}')
    write(rows, year, office, out)


if __name__ == '__main__':
    main()
