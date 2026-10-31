"""
Genera el catálogo de boletas y candidaturas de las elecciones federales (INE)
y lo escribe dentro de E2, entre los marcadores

    -- >>> ine_catalog.py
    -- <<< ine_catalog.py

Qué deduce de los archivos del INE, y cómo:

  · ballot_option: cada columna de votos del CSV de resultados, con los
    partidos que suma. "PT_MORENA_PES" o "PVEM-PT" son combinaciones.
  · candidacy: el catálogo de candidaturas trae, por distrito, a la persona
    candidata de cada partido y combinación. Las etiquetas con la MISMA persona
    son una coalición en ese territorio. Así salen solas las coaliciones
    parciales: en Tabasco 2024 PVEM, PT y MORENA fueron separados en
    diputaciones y senado, aunque en presidencia fueron juntos.
  · independientes: la columna de votos es genérica (CAND_IND_01, CI…) y la
    persona cambia por distrito. Se modelan como partidos IND1/IND2 y en cada
    distrito la candidatura lleva el nombre de quien compitió.

Uso (desde fuentes/):
  python ine_catalog.py <ruta a E2__elections_catalog.sql>
  python ine_catalog.py national <salida.sql> 2024:PRESIDENT 2021:FEDERAL_DEPUTY ...
      boleta y candidaturas de los 32 estados, para la base `electorama`

Sólo biblioteca estándar. Revisar el diff de E2 antes de correrlo.
"""

import io
import re
import sys
import zipfile
from collections import OrderedDict, defaultdict

STATE = '27'

ELECTIONS = [
    # (año, cargo, zip, zip interno, resultados, candidaturas)
    (2018, 'PRESIDENT', 'ine/20180708_2130_CW.zip', '20180708_2130_CW_presidencia.zip',
     '20180708_2130_CW_presidencia/presidencia.csv', '20180708_2130_CW_presidencia/presidencia_candidaturas_2018.csv'),
    (2018, 'SENATOR', 'ine/20180708_2130_CW.zip', '20180708_2130_CW_senadurias.zip',
     '20180708_2130_CW_senadurias/senadurias.csv', '20180708_2130_CW_senadurias/senadurias_candidaturas_2018.csv'),
    (2018, 'FEDERAL_DEPUTY', 'ine/20180708_2130_CW.zip', '20180708_2130_CW_diputaciones.zip',
     '20180708_2130_CW_diputaciones/diputaciones.csv', '20180708_2130_CW_diputaciones/diputaciones_candidaturas_2018.csv'),
    (2021, 'FEDERAL_DEPUTY', 'ine/20210611_1000_CW.zip', '20210611_1000_CW_diputaciones.zip',
     '20210611_1000_CW_diputaciones/diputaciones.csv', '20210611_1000_CW_diputaciones/diputaciones_candidaturas_2021.csv'),
    (2024, 'PRESIDENT', 'ine/20240608_2030_COMPUTOS.zip', '20240608_2030_COMPUTOS_PRES.zip',
     'PRES_2024.csv', 'PRES_CANDIDATURAS_2024.csv'),
    (2024, 'SENATOR', 'ine/20240608_2030_COMPUTOS.zip', '20240608_2030_COMPUTOS_SEN.zip',
     'SEN_2024.csv', 'SEN_CANDIDATURAS_2024.csv'),
    (2024, 'FEDERAL_DEPUTY', 'ine/20240608_2030_COMPUTOS.zip', '20240608_2030_COMPUTOS_DIP_FED.zip',
     'DIP_FED_2024.csv', 'DIP_FED_CANDIDATURAS_2024.csv'),
]

# Nombre en el archivo → código de partido. PES cambia de partido según el año.
LABEL_PARTY = {
    'PAN': 'PAN', 'PRI': 'PRI', 'PRD': 'PRD', 'PVEM': 'PVEM', 'PT': 'PT', 'MC': 'MC',
    'MORENA': 'MORENA', 'MOVIMIENTO CIUDADANO': 'MC', 'NUEVA ALIANZA': 'NA', 'NA': 'NA',
    'ENCUENTRO SOCIAL': 'PES', 'PES': 'PES', 'RSP': 'RSP', 'FXM': 'FXM', 'FS X MÉXICO': 'FXM',
}
PES = {2018: 'PES2018', 2021: 'PES2021'}

# Columnas de independientes en los resultados, en el orden en que el INE las numera.
INDEPENDENT_COLUMNS = {'CAND_IND_01': 'IND1', 'CAND_IND_02': 'IND2', 'CI': 'IND1',
                       'CANDIDATO/A INDEPENDIENTE': 'IND1'}

# Coaliciones con nombre propio, por año y partidos.
COALITION = {
    (2018, frozenset({'PT', 'MORENA', 'PES2018'})): ('JHH', 'Juntos Haremos Historia'),
    (2018, frozenset({'PAN', 'PRD', 'MC'})): ('PAF', 'Por México al Frente'),
    (2018, frozenset({'PRI', 'PVEM', 'NA'})): ('TPM', 'Todos por México'),
    (2021, frozenset({'PAN', 'PRI', 'PRD'})): ('VXM', 'Va por México'),
    (2021, frozenset({'PVEM', 'PT', 'MORENA'})): ('JHH', 'Juntos Hacemos Historia'),
    (2024, frozenset({'PAN', 'PRI', 'PRD'})): ('FCM', 'Fuerza y Corazón por México'),
    (2024, frozenset({'PVEM', 'PT', 'MORENA'})): ('SHH', 'Sigamos Haciendo Historia'),
}

ORDER = ['PAN', 'PRI', 'PRD', 'PVEM', 'PT', 'MC', 'NA', 'MORENA', 'PES2018', 'PES2021', 'RSP', 'FXM', 'IND1', 'IND2']


def party(label: str, year: int) -> str:
    label = label.strip().upper()
    code = LABEL_PARTY.get(label)
    if code is None:
        raise SystemExit(f'{year}: etiqueta sin partido: {label!r}')
    return PES.get(year, 'PES') if code == 'PES' else code


def parties(label: str, year: int) -> list:
    """'PT_MORENA_PES' / 'PVEM-PT-MORENA' / 'MOVIMIENTO CIUDADANO' → códigos."""
    label = label.strip()
    if label.upper() in LABEL_PARTY:
        return [party(label, year)]
    return sorted({party(p, year) for p in re.split(r'[_-]', label)}, key=ORDER.index)


def read(z, inner, member):
    with zipfile.ZipFile(z) as zf, zipfile.ZipFile(io.BytesIO(zf.read(inner))) as iz:
        return iz.read(member).decode('latin-1').splitlines()


def title(name: str) -> str:
    return ' '.join(w.capitalize() if len(w) > 2 else w.lower() for w in name.split())


def q(text) -> str:
    return "'" + str(text).replace("'", "''") + "'"


def arr(codes) -> str:
    return "'{" + ','.join(codes) + "}'"


def election_sql(year, office, z, inner, results, candidacies):
    # --- boleta: columnas de votos del CSV de resultados
    lines = read(z, inner, results)
    header = next(l for l in lines if l.startswith('CLAVE_CASILLA')).split('|')
    last = header.index('CNR') if 'CNR' in header else header.index('CANDIDATO/A NO REGISTRADO/A')
    options = []
    for label in header[header.index('PAN'):last]:
        if label in INDEPENDENT_COLUMNS:
            code = INDEPENDENT_COLUMNS[label]
            options.append((label, 'INDEPENDENT', [code], f'Independiente {code[-1]}'))
        else:
            codes = parties(label, year)
            kind = 'PARTY' if len(codes) == 1 else 'COMBINATION'
            options.append((label, kind, codes, '-'.join(codes)))

    # --- candidaturas: mismas personas = misma coalición, por territorio
    rows = [l.split('|') for l in read(z, inner, candidacies)[2:] if l]
    national = office == 'PRESIDENT'
    by_territory = defaultdict(lambda: OrderedDict())
    independent_seen = defaultdict(int)
    for r in rows:
        if national:
            # 2018: ESTADO|DISTRITO|PARTIDO_CI|CANDIDATURA  2024: ID_ENTIDAD|PARTIDO_CI|CANDIDATURA
            territory = None
            label, person = (r[2], r[3]) if len(r) >= 4 else (r[1], r[2])
        else:
            if r[0] != STATE:
                continue
            # senado: la candidatura es estatal aunque el INE la repita por distrito
            territory = None if office == 'SENATOR' else str(int(r[1]))
            label, person = r[2], r[3]
            if office == 'SENATOR' and r[1] != '1':
                continue
        if label.startswith('CI_'):
            independent_seen[territory] += 1
            codes = [f'IND{independent_seen[territory]}']
        else:
            codes = parties(label, year)
        group = by_territory[territory].setdefault(person, set())
        group.update(codes)

    candidacies_out = []
    for territory, groups in by_territory.items():
        for person, codes in groups.items():
            codes = sorted(codes, key=ORDER.index)
            named = COALITION.get((year, frozenset(codes)))
            if named:
                code, name = named
            elif len(codes) == 1:
                code, name = codes[0], codes[0] if not codes[0].startswith('IND') else 'Independiente'
            else:
                code, name = '_'.join(codes), '-'.join(codes)
            candidacies_out.append((territory, code, f'{name} ({title(person)})', codes))

    return options, candidacies_out


def national_sql(year, office, z, inner, results, candidacies):
    """
    Boleta y candidaturas de los 32 estados de una elección federal, como SQL
    que se corre directo contra `electorama` (ver load-national.sh).

    A diferencia de E2, la candidatura lleva el estado: en senado es estatal y
    en diputaciones es del distrito DENTRO del estado. Los independientes de un
    mismo distrito comparten la columna de votos (CI / CANDIDATO/A
    INDEPENDIENTE), así que se juntan en una sola candidatura IND1.
    """
    lines = read(z, inner, results)
    header = next(l for l in lines if l.startswith('CLAVE_CASILLA')).split('|')
    last = header.index('CNR') if 'CNR' in header else header.index('CANDIDATO/A NO REGISTRADO/A')
    options = []
    for label in header[header.index('PAN'):last]:
        if label in INDEPENDENT_COLUMNS:
            options.append((label, 'INDEPENDENT', ['IND1'], 'Independiente'))
        else:
            codes = parties(label, year)
            options.append((label, 'PARTY' if len(codes) == 1 else 'COMBINATION', codes, '-'.join(codes)))

    rows = [l.split('|') for l in read(z, inner, candidacies)[2:] if l]
    groups = defaultdict(lambda: OrderedDict())  # (estado, territorio) → persona → partidos
    for r in rows:
        if office == 'PRESIDENT':
            key, label, person = (None, None), r[1], r[2]
        else:
            if office == 'SENATOR' and r[1] != '1':
                continue  # el INE repite la fórmula estatal en cada distrito
            territory = None if office == 'SENATOR' else str(int(r[1]))
            key, label, person = (int(r[0]), territory), r[2], r[3]
        if label.startswith('CI_'):
            person, codes = 'Independiente', ['IND1']
        else:
            codes = parties(label, year)
        groups[key].setdefault(person, set()).update(codes)

    out = []
    for (state, territory), people in groups.items():
        for person, codes in people.items():
            codes = sorted(codes, key=ORDER.index)
            named = COALITION.get((year, frozenset(codes)))
            if named:
                code, name = named
            elif len(codes) == 1:
                code, name = codes[0], 'Independiente' if codes[0].startswith('IND') else codes[0]
            else:
                code, name = '_'.join(codes), '-'.join(codes)
            label = name if person == 'Independiente' else f'{name} ({title(person)})'
            out.append((state, territory, code, label, codes))

    election = f"(SELECT id FROM electoral_history.election WHERE year = {year} AND office = {q(office)} AND state_id IS NULL)"
    sql = [f'-- {office} {year}',
           'INSERT INTO electoral_history.ballot_option (election_id, raw_label, kind, party_codes, label) VALUES']
    sql.append(',\n'.join(f'  ({election}, {q(l)}, {q(k)}, {arr(c)}, {q(t)})' for l, k, c, t in options))
    sql.append('ON CONFLICT (election_id, raw_label) DO NOTHING;')
    sql.append(f'DELETE FROM electoral_history.candidacy WHERE election_id = {election};')
    sql.append('INSERT INTO electoral_history.candidacy (election_id, state_id, territory, code, name, party_codes) VALUES')
    sql.append(',\n'.join(
        f"  ({election}, {'NULL' if s is None else s}, {'NULL' if t is None else q(t)}, {q(code)}, {q(name)}, {arr(c)})"
        for s, t, code, name, c in out))
    sql.append(';')
    return '\n'.join(sql), len(out)


def national(year_offices, out_path):
    with open(out_path, 'w', encoding='utf-8', newline='\n') as f:
        f.write('-- generado por tools/normalize/ine_catalog.py national: no editar a mano\n'
                '\\set ON_ERROR_STOP on\nBEGIN;\n\n')
        for year, office, *files in ELECTIONS:
            if (year, office) in year_offices:
                sql, n = national_sql(year, office, *files)
                f.write(sql + '\n\n')
                print(f'{office} {year}: {n} candidaturas')
        f.write('COMMIT;\n')


def main():
    if sys.argv[1] == 'national':
        # python ine_catalog.py national <salida.sql> 2024:PRESIDENT 2021:FEDERAL_DEPUTY ...
        pairs = {(int(a.split(':')[0]), a.split(':')[1]) for a in sys.argv[3:]}
        national(pairs, sys.argv[2])
        return
    e2_path = sys.argv[1]
    option_rows, candidacy_rows = [], []
    for year, office, *files in ELECTIONS:
        options, candidacies = election_sql(year, office, *files)
        option_rows.append(f'  -- {office} {year}')
        option_rows += [f'  ({year}, {q(office)}, {q(l)}, {q(k)}, {arr(c)}, {q(t)}),' for l, k, c, t in options]
        candidacy_rows.append(f'  -- {office} {year}')
        candidacy_rows += [
            f"  ({year}, {q(office)}, {'NULL' if t is None else q(t)}, {q(code)}, {q(name)}, {arr(c)}),"
            for t, code, name, c in candidacies
        ]

    def values(rows):
        body = '\n'.join(rows)
        return body[: body.rstrip().rfind(',')] + ';' if body.rstrip().endswith(',') else body

    block = (
        '-- >>> ine_catalog.py  (generado: no editar a mano, correr tools/normalize/ine_catalog.py)\n'
        'INSERT INTO stg_option VALUES\n' + values(option_rows) + '\n\n'
        'INSERT INTO stg_candidacy VALUES\n' + values(candidacy_rows) + '\n'
        '-- <<< ine_catalog.py'
    )
    text = open(e2_path, encoding='utf-8').read()
    start, end = text.index('-- >>> ine_catalog.py'), text.index('-- <<< ine_catalog.py') + len('-- <<< ine_catalog.py')
    open(e2_path, 'w', encoding='utf-8', newline='\n').write(text[:start] + block + text[end:])
    print(f'{len(option_rows)} filas de boleta, {len(candidacy_rows)} de candidaturas')


if __name__ == '__main__':
    main()
