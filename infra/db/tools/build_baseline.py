"""Merge per-service pg_dump --schema-only files into one schema-per-service baseline.

usage: build_baseline.py <dumps_dir> <out.sql>
"""
import os, re, sys

import json

DUMPS, OUT = sys.argv[1], sys.argv[2]
# optional: {"schema": ["table", ...]} of unused legacy tables to leave out
EXCLUDE = set()
if len(sys.argv) > 3:
    for s, ts in json.load(open(sys.argv[3], encoding="utf-8")).items():
        EXCLUDE |= {(s, t) for t in ts}


def touches_excluded(body):
    for s, t in re.findall(r'(?:ALTER TABLE ONLY|ALTER TABLE|ON|OWNED BY|COMMENT ON TABLE|COMMENT ON COLUMN|CREATE TABLE|CREATE SEQUENCE)'
                           r' ([a-z_]+)\.("?[A-Za-z_][A-Za-z0-9_]*"?)', body):
        if (s, t.strip('"')) in EXCLUDE:
            return True
    return False

# dump file -> target schema for its `public` objects
SERVICES = {
    "geografia": "geography",
    "structure": "structure",
    "user": "users",
    "followers": "followers",
    "calendar": "calendar",
    "canvas_and_walls": "canvas_and_walls",
    "citizen_issues": "citizen_issues",
    "document_management": "document_management",
    "elections": "elections",
    "elections_results": "elections_results",
    "elections_structure": "elections_structure",
    "electoral_history": "electoral_history",
    "logfile": "logfile",
    "polls": "polls",
    "survey": "survey",
    "visits": "visits",
}
# view creation order: providers before consumers
VIEW_ORDER = ["geografia", "structure"]
DROP_SCHEMAS = {"topology"}  # owned by postgis_topology
FDW_DB = {"msvc_dash_" + k: k for k in SERVICES}

HDR = re.compile(r"^--\n-- Name: (.*?); Type: (.*?); Schema: (.*?); Owner: .*\n--\n", re.M)
SKIP_TYPES = {"EXTENSION", "SERVER", "USER MAPPING", "FOREIGN TABLE", "SCHEMA"}

PHASES = [
    ("types", {"TYPE", "DOMAIN"}),
    ("functions", {"FUNCTION", "PROCEDURE", "AGGREGATE"}),
    ("tables", {"SEQUENCE", "TABLE"}),
    ("owned", {"SEQUENCE OWNED BY", "DEFAULT"}),
    ("views", {"VIEW", "MATERIALIZED VIEW"}),
    ("constraints", {"CONSTRAINT"}),
    ("indexes", {"INDEX"}),
    ("fks", {"FK CONSTRAINT"}),
    ("triggers", {"TRIGGER", "RULE"}),
    ("comments", {"COMMENT"}),
]


def parse(path):
    text = open(path, encoding="utf-8").read()
    parts = HDR.split(text)
    entries = []
    for i in range(1, len(parts), 4):
        name, typ, schema, body = parts[i], parts[i + 1], parts[i + 2], parts[i + 3]
        body = re.sub(r"\n--\n-- PostgreSQL database dump complete\n--\n.*", "", body, flags=re.S)
        entries.append((name, typ, schema, body.strip()))
    return entries


def local_schema(svc_schema, schema):
    return svc_schema if schema == "public" else f"{svc_schema}_{schema}"


schemas = []
phase_out = {p: [] for p, _ in PHASES}
views_by_db = {}
unknown = set()
dropped = []
dropped_bodies = []
owned_seq = []

for db, svc in SERVICES.items():
    path = os.path.join(DUMPS, db + ".sql")
    if not os.path.exists(path):
        print("missing dump:", db)
        continue
    entries = parse(path)

    # foreign servers -> target db
    servers = {}
    for name, typ, _, body in entries:
        if typ == "SERVER":
            m = re.search(r"dbname '([^']+)'", body)
            servers[name] = FDW_DB.get(m.group(1)) if m else None
    # foreign tables -> real "schema.table" in merged db
    foreign = {}
    for name, typ, schema, body in entries:
        if typ != "FOREIGN TABLE":
            continue
        srv = re.search(r"\nSERVER (\w+)", body).group(1)
        rs = re.search(r"schema_name '([^']+)'", body).group(1)
        rt = re.search(r"table_name '([^']+)'", body).group(1)
        tdb = servers[srv]
        foreign[(schema, name.strip('"'))] = f'{local_schema(SERVICES[tdb], rs)}."{rt}"'
    fschemas = {s for s, _ in foreign}

    local = sorted({s for _, t, s, _ in entries if t == "SCHEMA" and False} |
                   {n for n, t, _, _ in entries if t == "SCHEMA"} - fschemas - DROP_SCHEMAS)
    schemas.append(svc)
    schemas += [local_schema(svc, s) for s in local]

    own_public = set()
    for name, typ, schema, _ in entries:
        if schema == "public" and typ in {"TABLE", "SEQUENCE", "VIEW", "MATERIALIZED VIEW",
                                          "FUNCTION", "TYPE", "DOMAIN", "PROCEDURE", "AGGREGATE"}:
            own_public.add(name.split("(")[0].strip('"'))

    def rewrite(body):
        def fsub(m):
            key = (m.group(1), m.group(3))
            if key not in foreign:
                unknown.add((db, m.group(0)))
                return m.group(0)
            return foreign[key]
        if fschemas:
            body = re.sub(r"\b(%s)\.(\"?)(\w+)\2" % "|".join(fschemas), fsub, body)
        def psub(m):
            ident = m.group(2) or m.group(3)
            if ident in own_public:
                return f"{svc}.{m.group(1)}"
            return m.group(0)
        body = re.sub(r"\bpublic\.((?:\"([^\"]+)\")|(\w+))", psub, body)
        for s in local:
            body = re.sub(r"(?<![\w.])%s\." % re.escape(s), local_schema(svc, s) + ".", body)
            body = re.sub(r"(?<![\w.])\"%s\"\." % re.escape(s), local_schema(svc, s) + ".", body)
        return body

    for name, typ, schema, body in entries:
        if typ in SKIP_TYPES or schema in fschemas or schema in DROP_SCHEMAS:
            continue
        if typ == "COMMENT" and (name.startswith("EXTENSION ") or name.startswith("SCHEMA ")):
            continue
        # stray dblink objects installed outside the extension; unused by the code
        if "'$libdir/" in body or name == "dblink_pkey_results":
            continue
        body = rewrite(body)
        if touches_excluded(body):
            dropped.append(f"{svc}: {typ} {name}")
            dropped_bodies.append(body)
            continue
        if typ == "SEQUENCE":
            owned_seq.append((svc, schema, name, body))
            continue
        if typ in {"FUNCTION", "PROCEDURE"}:
            own = local_schema(svc, schema)
            path_ = ", ".join(f"'{s}'" for s in dict.fromkeys([own, svc, "public", "pg_catalog"]))
            sp = f"    SET search_path TO {path_}\n"
            if re.search(r"^    SET search_path TO .*\n", body, re.M):
                body = re.sub(r"^    SET search_path TO .*\n", sp, body, count=1, flags=re.M)
            else:
                body = re.sub(r"^(    LANGUAGE \w+\n)", r"\1" + sp, body, count=1, flags=re.M)
        tag = f"-- {svc}: {typ} {name}"
        for p, types in PHASES:
            if typ in types:
                if p == "views":
                    views_by_db.setdefault(db, []).append(tag + "\n" + body)
                else:
                    phase_out[p].append(tag + "\n" + body)
                break
        else:
            raise SystemExit(f"unhandled type {typ} in {db}: {name}")

# sequences owned by excluded tables go too; the rest are created before any table
_drop_owned = set(re.findall(r"ALTER SEQUENCE ([a-z_]+\.\"?\w+\"?) OWNED BY", "\n".join(dropped_bodies)))
phase_out["tables"] = [f"-- {sv}: SEQUENCE {nm}\n{bd}" for sv, sc, nm, bd in owned_seq
                       if re.search(r"CREATE SEQUENCE ([a-z_]+\.\"?\w+\"?)", bd).group(1) not in _drop_owned
                       ] + phase_out["tables"]

for db in VIEW_ORDER + [d for d in SERVICES if d not in VIEW_ORDER]:
    phase_out["views"] += views_by_db.get(db, [])

with open(OUT, "w", encoding="utf-8", newline="\n") as f:
    f.write("-- Electorama: baseline schema (one schema per service).\n")
    f.write("-- Generated from the legacy msvc_dash_* databases; foreign tables replaced by\n")
    f.write("-- direct cross-schema references. Extensions are created in V0__extensions.sql.\n\n")
    f.write("SET LOCAL check_function_bodies = false;\n\n")
    used = "\n".join("\n".join(v) for v in phase_out.values())
    for s in schemas:
        if s in SERVICES.values() or re.search(r"\b%s\." % s, used):
            f.write(f"CREATE SCHEMA IF NOT EXISTS {s};\n")
    for p, _ in PHASES:
        f.write(f"\n\n-- ===== {p} =====\n\n")
        f.write("\n\n".join(phase_out[p]))
        f.write("\n")

print("schemas:", schemas)
print({p: len(v) for p, v in phase_out.items()})
print("dropped objects:", len(dropped))
if unknown:
    print("UNRESOLVED foreign refs:", sorted(unknown)[:20])
