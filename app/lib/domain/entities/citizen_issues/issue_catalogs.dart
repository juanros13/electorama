/// Ítem de catálogo simple (tipos, estatus, categorías, doc-categorías).
class CatItem {
  final int id;
  final String name;
  const CatItem({required this.id, required this.name});

  @override
  bool operator ==(Object other) =>
      other is CatItem && other.id == id && other.name == name;

  @override
  int get hashCode => Object.hash(id, name);
}

/// Recurso asociado a una categoría (filtrado por categoría en el form).
class IssueResource {
  final int id;
  final String name;
  final int categoryId;
  const IssueResource(
      {required this.id, required this.name, required this.categoryId});

  @override
  bool operator ==(Object other) =>
      other is IssueResource && other.id == id && other.name == name;

  @override
  int get hashCode => Object.hash(id, name);
}

/// Municipio (catálogo de Tabasco, entidad 27).
class IssueMunicipality {
  final int id;
  final String nomMun;
  const IssueMunicipality({required this.id, required this.nomMun});

  @override
  bool operator ==(Object other) => other is IssueMunicipality && other.id == id;

  @override
  int get hashCode => id.hashCode;
}
