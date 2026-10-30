"""
Escribe en E4 los totales OFICIALES estatales de las elecciones federales,
tal como los publica el INE en su sitio de cómputos (JSON por entidad):

  computosAAAA.ine.mx/assets/JSON/<ELECCIÓN>/ESTATAL/<Elección>_ESTATAL_27.json

guardados en fuentes/oficiales/. Se usa la votación de MAYORÍA RELATIVA
(totalActasMR / totalVotosMR cuando existen) y `votosCandidatoPartidoCoalicion`,
que es por candidatura — no la "con distribución", que reparte las
combinaciones con redondeos y además suma las actas de RP de las especiales.

Uso (desde fuentes/):
  python ine_official.py <ruta a E4__official_totals.sql>
"""

import json
import sys
from pathlib import Path

FILES = [
    (2018, 'PRESIDENT', 'oficiales/pres_2018.json'),
    (2018, 'SENATOR', 'oficiales/sen_2018.json'),
    (2018, 'FEDERAL_DEPUTY', 'oficiales/dip_2018.json'),
    (2021, 'FEDERAL_DEPUTY', 'oficiales/dip_2021.json'),
    (2024, 'PRESIDENT', 'oficiales/pres_2024.json'),
    (2024, 'SENATOR', 'oficiales/sen_2024.json'),
    (2024, 'FEDERAL_DEPUTY', 'oficiales/dip_2024.json'),
]

# Cómo nombra el INE cada candidatura → código de `candidacy` (ine_catalog.py).
CODES = {
    'PAN_PRD_MC': {2018: 'PAF'},
    'PRI_PVEM_NA': {2018: 'TPM'},
    'PT_MORENA_PES': {2018: 'JHH'},
    'PAN_PRI_PRD': {2021: 'VXM', 2024: 'FCM'},
    'PVEM_PT_MORENA': {2024: 'SHH'},
    'CI_39987': 'IND1',
    'CI_76991': 'IND2',
    'Candidaturas independiente': 'IND1',
    'NUEVA ALIANZA': 'NA',
    'MOVIMIENTO CIUDADANO': 'MC',
    'RSPPPN': 'RSP',
    'FS X MÉXICO': 'FXM',
    'PES': {2021: 'PES2021'},
    'CNR': 'NON_REGISTERED',
    'VN': 'NULL',
}


def code(label: str, year: int) -> str:
    mapped = CODES.get(label, label)
    if isinstance(mapped, dict):
        if year not in mapped:
            raise SystemExit(f'{year}: no sé qué candidatura es {label!r}')
        return mapped[year]
    return mapped


def main():
    e4_path = sys.argv[1]
    rows = []
    for year, office, path in FILES:
        j = json.loads(Path(path).read_text(encoding='utf-8'))
        stations = j.get('totalActasMR', j['totalActas'])
        total = j.get('totalVotosMR', j['totalVotos'])
        rows.append(f'  -- {office} {year}: {path} (corte {j.get("fechaCorte")} {j.get("horaCorte")})')
        rows.append(f"  ({year}, '{office}', NULL, 'STATIONS', {stations}),")
        rows.append(f"  ({year}, '{office}', NULL, 'NOMINAL_LIST', {j['listaNominal']}),")
        rows.append(f"  ({year}, '{office}', NULL, 'TOTAL', {total}),")
        for c in j['votosCandidatoPartidoCoalicion']:
            label = c.get('siglasPartido') or c.get('nombrePartido')
            rows.append(f"  ({year}, '{office}', NULL, '{code(label, year)}', {c['total']}),")

    body = '\n'.join(rows)
    body = body[: body.rfind(',')] + ';'
    block = (
        '-- >>> ine_official.py  (generado: no editar a mano, correr tools/normalize/ine_official.py)\n'
        'INSERT INTO stg_official (year, office, municipality_cve, code, value) VALUES\n' + body + '\n'
        '-- <<< ine_official.py'
    )
    text = open(e4_path, encoding='utf-8').read()
    start = text.index('-- >>> ine_official.py')
    end = text.index('-- <<< ine_official.py') + len('-- <<< ine_official.py')
    open(e4_path, 'w', encoding='utf-8', newline='\n').write(text[:start] + block + text[end:])
    print(f'{len(rows)} filas')


if __name__ == '__main__':
    main()
