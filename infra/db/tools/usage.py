"""Report which tables of the merged baseline are referenced by code.

usage: usage.py <repo_root> <baseline.sql>  -> prints JSON {schema: {"keep": [...], "drop": [...]}}
"""
import json, os, re, sys

ROOT, BASE = sys.argv[1], sys.argv[2]
EXT = {".java", ".ts", ".html", ".json", ".dart", ".jrxml", ".xml", ".yml", ".yaml", ".sql", ".py"}
SKIP_DIRS = {"node_modules", "target", "build", ".dart_tool", ".angular", "dist", ".git"}

tokens = set()
camel = re.compile(r"(?<=[a-z0-9])(?=[A-Z])")
for dp, dn, fn in os.walk(ROOT):
    dn[:] = [d for d in dn if d not in SKIP_DIRS]
    if "/src/main/resources/db" in dp.replace("\\", "/"):
        continue  # legacy migration scripts mention backups/legacy tables
    for f in fn:
        if os.path.splitext(f)[1] not in EXT:
            continue
        try:
            s = open(os.path.join(dp, f), encoding="utf-8").read()
        except Exception:
            continue
        for w in re.findall(r"[A-Za-z_][A-Za-z0-9_]*", s):
            tokens.add(w.lower())
            if any(c.isupper() for c in w):
                tokens.add(camel.sub("_", w).lower())
                base = re.sub(r"(Entity|Dto|Repository)$", "", w)
                tokens.add(camel.sub("_", base).lower())

sql = open(BASE, encoding="utf-8").read()
blocks = re.split(r"\n(?=-- [a-z_]+: )", sql)
tables = {}
deps = []
for b in blocks:
    m = re.match(r"-- ([a-z_]+): (TABLE|VIEW|MATERIALIZED VIEW|FUNCTION|TRIGGER|FK CONSTRAINT|DEFAULT|CONSTRAINT|INDEX|SEQUENCE OWNED BY|SEQUENCE|COMMENT) (.*)", b)
    if not m:
        continue
    svc, typ, name = m.groups()
    body = b.split("\n", 1)[1] if "\n" in b else ""
    t = re.match(r'CREATE (?:UNLOGGED )?TABLE ([a-z_]+)\.("?[^ (]+"?)', body)
    if typ == "TABLE" and t:
        tables[(t.group(1), t.group(2).strip('"'))] = body
    elif typ in {"VIEW", "MATERIALIZED VIEW", "FUNCTION", "TRIGGER", "FK CONSTRAINT"}:
        deps.append((typ, name, body))

def refs(body):
    return {(s, n.strip('"')) for s, n in re.findall(r'\b([a-z_]+)\.("?[A-Za-z_][A-Za-z0-9_]*"?)', body)}

keep = {k for k in tables if k[1].lower() in tokens}
# objects that survive keep the tables they reference; repeat to closure
changed = True
while changed:
    changed = False
    for typ, name, body in deps:
        r = refs(body) & set(tables)
        if typ == "FK CONSTRAINT":
            own = re.search(r"ALTER TABLE ONLY ([a-z_]+)\.(\"?\w+\"?)", body)
            if not own or (own.group(1), own.group(2).strip('"')) not in keep:
                continue
        # functions / triggers: kept wholesale, so keep what they touch
        new = r - keep
        if new:
            keep |= new
            changed = True

out = {}
for (s, n) in sorted(tables):
    out.setdefault(s, {"keep": [], "drop": []})["keep" if (s, n) in keep else "drop"].append(n)
print(json.dumps(out, indent=1, ensure_ascii=False))
