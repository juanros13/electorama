"""
Genera el catálogo de boletas y candidaturas de las elecciones LOCALES (IEPCT)
y lo escribe dentro de E2, entre los marcadores

    -- >>> iepct_catalog.py
    -- <<< iepct_catalog.py

El IEPCT no publica un catálogo de candidaturas, pero su archivo por casilla
deja VACÍA la celda de una opción que no estaba en la boleta de ese
territorio (se comprobó contra los resúmenes: la coalición PAN-PRI de 2021
aparece en 13 municipios y en los otros 4 la celda está vacía). Entonces, por
territorio (municipio en ayuntamientos, distrito local en diputaciones, el
estado en gubernatura):

  · las opciones presentes son las que estaban en la boleta;
  · una combinación presente ("PVEM Y MORENA") une a sus partidos en una sola
    candidatura; los partidos que no quedan unidos van solos;
  · cada independiente ("C. IND. …") es su propia candidatura donde aparece.

Lee los st_/vt_ que dejó normalize.py, así que hay que normalizar primero.

Uso (desde C:\\Users\\juanr\\geo-2026\\electoral-history):
  python iepct_catalog.py <ruta a E2__elections_catalog.sql>
"""

import csv
import re
import sys
from collections import defaultdict

ELECTIONS = [(2018, 'GOVERNOR'), (2024, 'GOVERNOR'),
             (2018, 'LOCAL_DEPUTY'), (2021, 'LOCAL_DEPUTY'), (2024, 'LOCAL_DEPUTY'),
             (2018, 'MAYOR'), (2021, 'MAYOR'), (2024, 'MAYOR')]

TOKEN = {'PAN': 'PAN', 'PRI': 'PRI', 'PRD': 'PRD', 'PVEM': 'PVEM', 'PT': 'PT', 'MC': 'MC',
         'MORENA': 'MORENA', 'NA': 'NA', 'NVA_ALIANZA': 'NA', 'ES': 'PES2018', 'RSP': 'RSP',
         'FPM': 'FXM'}
PES = {2018: 'PES2018', 2021: 'PES2021'}
ORDER = ['PAN', 'PRI', 'PRD', 'PVEM', 'PT', 'MC', 'NA', 'MORENA', 'PES2018', 'PES2021', 'RSP', 'FXM']

MUNICIPALITY = {
    'BALANCAN': 1, 'CARDENAS': 2, 'CENTLA': 3, 'CENTRO': 4, 'COMALCALCO': 5, 'CUNDUACAN': 6,
    'EMILIANO ZAPATA': 7, 'HUIMANGUILLO': 8, 'JALAPA': 9, 'JALPA DE MENDEZ': 10, 'JONUTA': 11,
    'MACUSPANA': 12, 'NACAJUCA': 13, 'PARAISO': 14, 'TACOTALPA': 15, 'TEAPA': 16, 'TENOSIQUE': 17,
}


def plain(text: str) -> str:
    return text.upper().translate(str.maketrans('ÁÉÍÓÚÜÑ', 'AEIOUUN')).strip()


def municipality(name: str) -> int:
    return MUNICIPALITY[plain(name)]


def is_independent(label: str) -> bool:
    return plain(label).startswith(('C. IND.', 'CAND. IND.'))


def parties(label: str, year: int) -> list:
    label = plain(label)
    if label == 'NVA_ALIANZA':
        return ['NA']
    codes = set()
    for token in re.split(r'_|,| Y ', label):
        token = token.strip()
        if token == 'PES':
            codes.add(PES[year])
        elif token in TOKEN:
            codes.add(TOKEN[token])
        else:
            raise SystemExit(f'{year}: no conozco el partido {token!r} de {label!r}')
    return sorted(codes, key=ORDER.index)


def title(name: str) -> str:
    return ' '.join(w.capitalize() if len(w) > 2 else w.lower() for w in name.split())


def q(text) -> str:
    return "'" + str(text).replace("'", "''") + "'"


def arr(codes) -> str:
    return "'{" + ','.join(codes) + "}'"


def catalog(year: int, office: str):
    stations = {}
    with open(f'st_{year}_{office}.txt', encoding='utf-8') as f:
        for r in csv.reader(f, delimiter='|'):
            key, district, mun = r[0], r[2], r[3]
            if office == 'MAYOR':
                stations[key] = str(municipality(mun))
            elif office == 'LOCAL_DEPUTY':
                stations[key] = district
            else:
                stations[key] = None

    labels, present = [], defaultdict(set)
    with open(f'vt_{year}_{office}.txt', encoding='utf-8') as f:
        for key, label, _ in csv.reader(f, delimiter='|'):
            if label not in labels:
                labels.append(label)
            present[stations[key]].add(label)

    # --- boleta
    independents = {}
    options = []
    for label in labels:
        if plain(label).startswith('TOTAL '):
            options.append((label, 'DERIVED_TOTAL', parties(label[6:], year), label))
        elif is_independent(label):
            code = f'IND{len(independents) + 1}'
            independents[label] = code
            name = re.sub(r'^(C\.|CAND\.) IND\. ', '', ' '.join(label.split()), flags=re.I)
            options.append((label, 'INDEPENDENT', [code], f'Independiente ({title(name)})'))
        else:
            codes = parties(label, year)
            options.append((label, 'PARTY' if len(codes) == 1 else 'COMBINATION', codes, '-'.join(codes)))
    option = {o[0]: o for o in options}

    # --- candidaturas por territorio
    candidacies = []
    for territory in sorted(present, key=lambda t: (t is not None, int(t) if t else 0)):
        on_ballot = present[territory]
        parent = {}

        def find(x):
            while parent[x] != x:
                parent[x] = parent[parent[x]]
                x = parent[x]
            return x

        for label in on_ballot:
            kind, codes = option[label][1], option[label][2]
            if kind in ('PARTY', 'COMBINATION'):
                for c in codes:
                    parent.setdefault(c, c)
                for c in codes[1:]:
                    parent[find(c)] = find(codes[0])
        groups = defaultdict(list)
        for c in parent:
            groups[find(c)].append(c)
        for members in groups.values():
            members = sorted(members, key=ORDER.index)
            code = '_'.join(members)
            candidacies.append((territory, code, '-'.join(members), members))
        for label in on_ballot:
            if option[label][1] == 'INDEPENDENT':
                candidacies.append((territory, option[label][2][0], option[label][3], option[label][2]))
    return options, candidacies


def main():
    e2_path = sys.argv[1]
    option_rows, candidacy_rows = [], []
    for year, office in ELECTIONS:
        options, candidacies = catalog(year, office)
        option_rows.append(f'  -- {office} {year}')
        option_rows += [f'  ({year}, {q(office)}, {q(l)}, {q(k)}, {arr(c)}, {q(t)}),' for l, k, c, t in options]
        candidacy_rows.append(f'  -- {office} {year}')
        candidacy_rows += [
            f"  ({year}, {q(office)}, {'NULL' if t is None else q(t)}, {q(code)}, {q(name)}, {arr(c)}),"
            for t, code, name, c in candidacies
        ]

    def values(rows):
        body = '\n'.join(rows)
        return body[: body.rfind(',')] + ';'

    block = (
        '-- >>> iepct_catalog.py  (generado: no editar a mano, correr tools/normalize/iepct_catalog.py)\n'
        'INSERT INTO stg_option VALUES\n' + values(option_rows) + '\n\n'
        'INSERT INTO stg_candidacy VALUES\n' + values(candidacy_rows) + '\n'
        '-- <<< iepct_catalog.py'
    )
    text = open(e2_path, encoding='utf-8').read()
    start = text.index('-- >>> iepct_catalog.py')
    end = text.index('-- <<< iepct_catalog.py') + len('-- <<< iepct_catalog.py')
    open(e2_path, 'w', encoding='utf-8', newline='\n').write(text[:start] + block + text[end:])
    print(f'{len(option_rows)} filas de boleta, {len(candidacy_rows)} de candidaturas')


if __name__ == '__main__':
    main()
