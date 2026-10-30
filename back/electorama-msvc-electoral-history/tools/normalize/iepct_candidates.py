"""
Nombres de las personas que encabezaron cada candidatura LOCAL (IEPCT) y los
escribe dentro de E2, entre los marcadores

    -- >>> iepct_candidates.py
    -- <<< iepct_candidates.py

El archivo por casilla del IEPCT no trae quién encabezó cada partido o
coalición; los nombres salen de las listas de registro del propio IEPCT:

  2021  iepctabasco.mx/docs/proceso_2021/candidatos/ (corte al 05/06/2021, un
        día antes de la jornada, ya con sustituciones):
          candidaturas_de_diputaciones_principio_de_mayoria_relativa_050621.pdf
          presidencias_municipales_y_regidurias_mayoria_relativa_050421.pdf
        Se guardan como dip_mr_2021.pdf y ayu_mr_2021.pdf. Se leen con
        pdftotext (Xpdf 4): -raw da los renglones en orden y -table la columna
        SOBRENOMBRE, que en -raw viene pegada al nombre.
  2024  Conóceles del IEPCT (conoceles.iepctabasco.mx): la tabla de la página
        (candidatura/getCandidaturas) trae el distrito, que la exportación
        exceldb/1 no trae. Se guardó en conoceles_2024_mr.txt:
        elección|ámbito|actor político|nombre, sólo mayoría relativa, fórmula 1,
        propietaria(o).
  2018  El IEPCT no publica las listas; esas candidaturas se quedan sin nombre.

En ayuntamientos la primera fórmula propietaria de cada partido en cada
municipio es la presidencia municipal. Las independientes ya traen su nombre
desde la boleta y aquí se saltan.

Uso (desde C:\\Users\\juanr\\geo-2026\\electoral-history\\candidatos):
  python iepct_candidates.py <ruta a E2__elections_catalog.sql>
"""

import re
import subprocess
import sys
from pathlib import Path

MUNICIPALITY = {
    'BALANCAN': 1, 'CARDENAS': 2, 'CENTLA': 3, 'CENTRO': 4, 'COMALCALCO': 5, 'CUNDUACAN': 6,
    'EMILIANO ZAPATA': 7, 'HUIMANGUILLO': 8, 'JALAPA': 9, 'JALPA DE MENDEZ': 10, 'JONUTA': 11,
    'MACUSPANA': 12, 'NACAJUCA': 13, 'PARAISO': 14, 'TACOTALPA': 15, 'TEAPA': 16, 'TENOSIQUE': 17,
}
PARTY = {
    'PARTIDO ACCION NACIONAL': 'PAN', 'PARTIDO REVOLUCIONARIO INSTITUCIONAL': 'PRI',
    'PARTIDO DE LA REVOLUCION DEMOCRATICA': 'PRD', 'PARTIDO VERDE ECOLOGISTA DE MEXICO': 'PVEM',
    'PARTIDO DEL TRABAJO': 'PT', 'PARTIDO MOVIMIENTO CIUDADANO': 'MC', 'PARTIDO MORENA': 'MORENA',
    'PARTIDO ENCUENTRO SOLIDARIO': 'PES2021', 'PARTIDO REDES SOCIALES PROGRESISTAS': 'RSP',
    'PARTIDO FUERZA POR MEXICO': 'FXM',
}
ORDER = ['PAN', 'PRI', 'PRD', 'PVEM', 'PT', 'MC', 'NA', 'MORENA', 'PES2018', 'PES2021', 'RSP', 'FXM']
AMBITO = (r'(?:\d{2} DISTRITO ELECTORAL )?(?:BALANCÁN|CÁRDENAS|CENTLA|CENTRO|COMALCALCO|CUNDUACÁN|'
          r'EMILIANO ZAPATA|HUIMANGUILLO|JALAPA|JALPA DE MÉNDEZ|JONUTA|MACUSPANA|NACAJUCA|PARAÍSO|'
          r'TACOTALPA|TEAPA|TENOSIQUE)')


def plain(text: str) -> str:
    return text.upper().translate(str.maketrans('ÁÉÍÓÚÜÑ', 'AEIOUUN')).strip()


def title(name: str) -> str:
    """Igual que ine_catalog.py: 'JUAN DE LA CRUZ' → 'Juan de la Cruz'."""
    return ' '.join(w.capitalize() if len(w) > 2 else w.lower() for w in name.split())


def parties(actor: str):
    """'PARTIDO MORENA' → [MORENA]; 'VA X TABASCO (PAN y PRI)' → [PAN, PRI];
    independiente → None."""
    actor = plain(actor)
    if actor in PARTY:
        return [PARTY[actor]]
    m = re.search(r'\(([^()]*)\)\s*$', actor)
    if m and not actor.startswith('(CIND)'):
        codes = [c.strip() for c in re.split(r',| Y ', m.group(1))]
        if all(c in ORDER for c in codes):
            return sorted(codes, key=ORDER.index)
    if actor.startswith('(CIND)') or actor not in PARTY:
        return None


# ---------------------------------------------------------------- 2021 (PDF)

def pdftext(pdf: Path, mode: str) -> str:
    return subprocess.run(['pdftotext', mode, '-enc', 'UTF-8', str(pdf), '-'],
                          capture_output=True, check=True).stdout.decode('utf-8')


def pdf_rows(pdf: Path):
    """(número, ámbito, actor, tipo, nombre y sobrenombre) en el orden del listado."""
    s = ' '.join(pdftext(pdf, '-raw').split())
    s = re.sub(r'INSTITUTO ELECTORAL Y DE PARTICIPACIÓN.*?G[ÉE]NERO', ' ', s)
    out, pos, n = [], 0, 1
    while True:
        m = re.compile(
            r'(?<![\d\w])' + str(n) + r' +(' + AMBITO + r') (.+?) (PROPIETARI[OA]|SUPLENTE)'
            r'(?: (.*?))?(?= *' + str(n + 1) + r' +' + AMBITO + r' | *RENUNCIAS| *$)').search(s, pos)
        if not m:
            break
        rest = re.sub(r'(?:^| )[MH]$', '', (m.group(4) or '').strip()).strip()
        out.append((n, m.group(1), m.group(2), m.group(3), rest))
        pos, n = m.end(), n + 1
    return out


def nickname_words(pdf: Path):
    """Palabras de la columna SOBRENOMBRE, en orden (en -table las columnas quedan alineadas)."""
    words = []
    for page in pdftext(pdf, '-table').split('\f'):
        lines = page.splitlines()
        head = next((l for l in lines if 'SOBRENOMB' in l), None)
        if head is None:
            continue
        start = head.index('SOBRENOMB') - 2
        for line in lines:
            if line is head:
                continue
            for m in re.finditer(r'\S+(?: \S+)*', line):
                if m.start() >= start and m.group() not in ('M', 'H', 'RE'):
                    words += re.sub(r'\s+[MH]$', '', m.group()).split()
    return words


def without_nickname(rows, words):
    """El sufijo del nombre que aparece a continuación en la columna SOBRENOMBRE
    es el sobrenombre. Un nombre completo lleva al menos tres palabras, así que
    un apellido que se partió en dos renglones no se toma por sobrenombre."""
    ptr, out = 0, []
    for n, ambito, actor, kind, rest in rows:
        toks, nick = rest.split(), 0
        for k in range(min(len(toks) - 3, 6), 0, -1):
            window = words[ptr:ptr + k + 2]
            hit = next((i for i in range(len(window) - k + 1) if window[i:i + k] == toks[-k:]), None)
            if hit is not None:
                nick, ptr = k, ptr + hit + k
                break
        name = ' '.join(toks[:len(toks) - nick])
        # un sobrenombre entre comillas ("EL QUIMICO") a veces sí queda en la columna del nombre
        name = re.sub(r'\s*["“”][^"“”]*["“”]?\s*$', '', name).strip()
        out.append((n, ambito, actor, kind, name))
    return out


def from_pdf(pdf: Path, year: int, office: str):
    heads, seen = [], set()
    for n, ambito, actor, kind, name in without_nickname(pdf_rows(pdf), nickname_words(pdf)):
        if not kind.startswith('PROPIETARI') or (ambito, actor) in seen:
            continue
        seen.add((ambito, actor))
        codes = parties(actor)
        if codes is None or not name:
            continue
        if office == 'LOCAL_DEPUTY':
            territory = str(int(ambito[:2]))
        else:
            territory = str(MUNICIPALITY[plain(ambito)])
        heads.append((year, office, territory, codes, name))
    return heads


# -------------------------------------------------------- 2024 (Conóceles)

def from_conoceles(path: Path):
    office_of = {'GUB': 'GOVERNOR', 'DIP': 'LOCAL_DEPUTY', 'PMR': 'MAYOR'}
    heads = []
    for line in path.read_text(encoding='utf-8').splitlines()[1:]:
        election, ambito, actor, name = line.split('|')
        codes = parties(actor)
        if codes is None:
            continue
        office = office_of[election]
        if office == 'GOVERNOR':
            territory = None
        elif office == 'LOCAL_DEPUTY':
            territory = str(int(re.match(r'DISTRITO (\d+)', ambito).group(1)))
        else:
            territory = str(MUNICIPALITY[plain(ambito)])
        heads.append((2024, office, territory, codes, name))
    return heads


def q(text) -> str:
    return "'" + str(text).replace("'", "''") + "'"


def main():
    e2_path = sys.argv[1]
    here = Path.cwd()
    heads = (from_pdf(here / 'dip_mr_2021.pdf', 2021, 'LOCAL_DEPUTY')
             + from_pdf(here / 'ayu_mr_2021.pdf', 2021, 'MAYOR')
             + from_conoceles(here / 'conoceles_2024_mr.txt'))

    rows = [
        f"  ({y}, {q(o)}, {'NULL' if t is None else q(t)}, '{{{','.join(c)}}}', {q(title(p))}),"
        for y, o, t, c, p in heads
    ]
    body = '\n'.join(rows)
    block = (
        '-- >>> iepct_candidates.py  (generado: no editar a mano, correr tools/normalize/iepct_candidates.py)\n'
        'INSERT INTO stg_person VALUES\n' + body[: body.rfind(',')] + ';\n'
        '-- <<< iepct_candidates.py'
    )
    text = open(e2_path, encoding='utf-8').read()
    start = text.index('-- >>> iepct_candidates.py')
    end = text.index('-- <<< iepct_candidates.py') + len('-- <<< iepct_candidates.py')
    open(e2_path, 'w', encoding='utf-8', newline='\n').write(text[:start] + block + text[end:])
    print(f'{len(rows)} personas')


if __name__ == '__main__':
    main()
