"""Dump small configuration catalogs from the legacy DBs as a Flyway seed.

usage: build_catalogs.py <out.sql>
"""
import os, subprocess, sys

OUT = sys.argv[1]
TERRITORY_ORDER = """(with recursive d as (
  select id, 0 lvl from territory where parent_territory_id is null
  union all select t.id, d.lvl + 1 from territory t join d on t.parent_territory_id = d.id)
 select lvl from d where d.id = t.id), t.id"""
# (legacy db suffix, new schema, [(table, order by)]) in FK order
CATALOGS = [
    ("user", "users", ["menus", "menus_groups", "menus_roles"]),
    ("elections", "elections", [
        "elections_years", "elections_types", "elections_positions", "political_parties_type",
        "political_parties", "elections", "polling_booths_categories", "polling_booths_address_type",
        "polling_booths_status", "polling_booths_types", "polling_booths_issues_categories",
        "polling_booths_issues_sub_categories", "status_validate"]),
    ("elections_structure", "elections_structure", ["cat_sex", "pollings_representatives_types"]),
    ("canvas_and_walls", "canvas_and_walls", ["canvas_and_walls_category", "canvas_and_walls_proyects"]),
    ("structure", "structure", [("territory", TERRITORY_ORDER), "campaign", "structure", "campaign_election"]),
]
env = dict(os.environ, PGPASSWORD=os.environ.get("PGPASSWORD", "activo"), PGCLIENTENCODING="UTF8")


def psql(db, sql):
    return subprocess.run(["psql", "-h", "localhost", "-U", "postgres", "-d", db, "-Atc", sql],
                          env=env, capture_output=True, text=True, encoding="utf-8", check=True).stdout


parts = ["-- Electorama: catálogos de configuración (menús, tipos, estados, campaña).",
         "-- Exportados de las bases msvc_dash_* el 2026-10-30. Sin datos personales.",
         "-- Cada fila va como JSON para no depender del orden de columnas.", ""]
for db, schema, tables in CATALOGS:
    for t in tables:
        t, order = t if isinstance(t, tuple) else (t, "1")
        rows = psql(f"msvc_dash_{db}", f"select to_jsonb(t)::text from public.{t} t order by {order}").splitlines()
        parts.append(f"-- {schema}.{t}: {len(rows)} filas")
        if rows:
            values = ",\n".join("('%s')" % r.replace("'", "''") for r in rows)
            parts.append(f"INSERT INTO {schema}.{t}\nSELECT r.* FROM (VALUES\n{values}\n) v(j),"
                         f" jsonb_populate_record(NULL::{schema}.{t}, v.j::jsonb) r;")
        # keep sequences ahead of the seeded ids
        for seq, col in [l.split("|") for l in psql(f"msvc_dash_{db}", f"""
                select pg_get_serial_sequence('public.{t}', a.attname), a.attname from pg_attribute a
                where a.attrelid = 'public.{t}'::regclass and a.attnum > 0 and not a.attisdropped
                  and pg_get_serial_sequence('public.{t}', a.attname) is not null""").splitlines()]:
            seq = seq.replace("public.", schema + ".", 1)
            parts.append(f"SELECT setval('{seq}', coalesce((SELECT max({col}) FROM {schema}.{t}), 0) + 1, false);")
        parts.append("")
        print(f"{schema}.{t}: {len(rows)}")
open(OUT, "w", encoding="utf-8", newline="\n").write("\n".join(parts))
