#!/usr/bin/env bash
# Copia los datos de referencia (geografía INE, historial electoral, catálogos
# electorales) de las bases legadas msvc_dash_* a la base nueva `electorama`.
#
# No copia datos personales ni operativos (usuarios, simpatizantes, personas,
# encuestas, incidencias). Requiere que Flyway ya haya aplicado V0..V2.
#
#   LEGACY_PGHOST=localhost LEGACY_PGUSER=postgres LEGACY_PGPASSWORD=... \
#   PGHOST=localhost PGPORT=5432 PGUSER=electorama PGPASSWORD=... PGDATABASE=electorama \
#   ./infra/db/reference/load-reference.sh
set -euo pipefail

: "${LEGACY_PGHOST:=localhost}" "${LEGACY_PGPORT:=5432}" "${LEGACY_PGUSER:=postgres}"
: "${LEGACY_DB_PREFIX:=msvc_dash_}"

# legacy db | schema nuevo | tablas en orden de llaves foráneas
TABLES=(
  "geografia|geography|geo_entidades geo_municipios geo_distritos_federales geo_distritos_locales geo_secciones geo_cp geo_colonias geo_localidades_juntos"
  "electoral_history|electoral_history|source election municipality section_municipality party bloc bloc_party ballot_option candidacy polling_station station_vote official_total"
  "elections|elections|elections_federal_district elections_local_district elections_municipality elections_voter_list geo_secciones polling_booths polling_booths_election_association candidates candidates_party_associations"
  "followers|followers|geo_municipios geo_distritos_federales geo_distritos_locales geo_secciones polling_booths cat_committee cat_sex cat_marital_status cat_family_role cat_section"
  "user|users|geo_secciones"
  "citizen_issues|citizen_issues|geo_municipios geo_localidades_juntos"
  "survey|survey|localidades secciones_ok"
)

legacy_dump() { # db table
  PGPASSWORD="${LEGACY_PGPASSWORD:-}" pg_dump -h "$LEGACY_PGHOST" -p "$LEGACY_PGPORT" -U "$LEGACY_PGUSER" \
    -d "${LEGACY_DB_PREFIX}$1" --data-only --no-owner --no-privileges -t "public.\"$2\""
}

for entry in "${TABLES[@]}"; do
  IFS='|' read -r db schema tables <<<"$entry"
  for t in $tables; do
    # sólo carga tablas vacías: así se puede re-ejecutar sin pisar nada
    if [ "$(psql -X -Atc "SELECT EXISTS (SELECT 1 FROM ${schema}.\"${t}\")")" = "t" ]; then
      echo "-- ${schema}.${t} ya tiene datos, se omite"
      continue
    fi
    echo ">> ${LEGACY_DB_PREFIX}${db}.${t} -> ${schema}.${t}"
    {
      echo "BEGIN;"
      # sólo se reescriben las líneas de control, nunca los datos del COPY
      legacy_dump "$db" "$t" | sed -E \
        -e "s/^COPY public\./COPY ${schema}./" \
        -e "s/^SELECT pg_catalog\.setval\('public\./SELECT pg_catalog.setval('${schema}./"
      echo "COMMIT;"
    } | psql -v ON_ERROR_STOP=1 -q -X
  done
done
echo "Datos de referencia cargados."
