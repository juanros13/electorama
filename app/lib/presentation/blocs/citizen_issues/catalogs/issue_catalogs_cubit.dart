import 'dart:convert';

import 'package:equatable/equatable.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/domain/entities/citizen_issues/issue_catalogs.dart';

/// Carga los catálogos de incidencias (snapshot en assets/citizen_issues) a
/// memoria, para captura offline. Tipos/estatus/categorías/doc-categorías,
/// recursos (por categoría) y municipios de Tabasco.
class IssueCatalogsCubit extends Cubit<IssueCatalogsState> {
  IssueCatalogsCubit() : super(const IssueCatalogsState());

  // Categorías que habilitan campos condicionales (igual que el front).
  static const beneficiaryCategories = {
    'PROGRAMAS BIENESTAR',
    'PROGRAMAS JCF/BECAS',
  };
  static const resourceCategories = {
    'INSUMOS MEDICOS',
    'APOYO VARIOS (COMPRAS)',
    'EDUCACIÓN',
  };
  static const fileCategories = {
    'INSUMOS MEDICOS',
    'GESTIONES MEDICAS',
    'APOYO JURIDICO',
    'MEDICAMENTO',
  };

  Future<void> load() async {
    if (state.loaded) return;
    const dir = 'assets/citizen_issues';

    List<dynamic> wrap(String raw, String key) =>
        (jsonDecode(raw) as Map<String, dynamic>)[key] as List<dynamic>;

    final typesRaw = await rootBundle.loadString('$dir/catIssueTypes.json');
    final statusRaw = await rootBundle.loadString('$dir/catIssueStatus.json');
    final catRaw = await rootBundle.loadString('$dir/catIssueCategory.json');
    final docRaw =
        await rootBundle.loadString('$dir/catIssueSolutionDocumentsCategory.json');
    final resRaw = await rootBundle.loadString('$dir/resources.json');
    final munRaw = await rootBundle.loadString('$dir/municipalities.json');

    CatItem toCat(dynamic e) =>
        CatItem(id: e['id'] as int, name: e['name'] as String);

    final types = wrap(typesRaw, 'catIssueTypes').map(toCat).toList();
    final statuses = wrap(statusRaw, 'catIssueStatus').map(toCat).toList();
    final categories = wrap(catRaw, 'catIssueCategory').map(toCat).toList();

    // doc-categorías vienen duplicadas en el backend; deduplicar por id.
    final docSeen = <int>{};
    final docCategories = <CatItem>[];
    for (final e in wrap(docRaw, 'catIssueSolutionDocumentsCategory')) {
      final c = toCat(e);
      if (docSeen.add(c.id)) docCategories.add(c);
    }

    final resources = (jsonDecode(resRaw) as List<dynamic>)
        .map((e) => IssueResource(
              id: e['id'] as int,
              name: e['name'] as String,
              categoryId: (e['issueCategory']?['id'] as int?) ?? 0,
            ))
        .toList();

    final municipalities = wrap(munRaw, 'municipios')
        .map((e) => IssueMunicipality(
            id: e['id'] as int, nomMun: e['nomMun'] as String))
        .toList()
      ..sort((a, b) => a.nomMun.compareTo(b.nomMun));

    emit(IssueCatalogsState(
      loaded: true,
      types: types,
      statuses: statuses,
      categories: categories,
      docCategories: docCategories,
      resources: resources,
      municipalities: municipalities,
    ));
  }

  List<IssueResource> resourcesForCategory(int categoryId) =>
      state.resources.where((r) => r.categoryId == categoryId).toList();

  bool needsBeneficiary(String? categoryName) =>
      categoryName != null && beneficiaryCategories.contains(categoryName);
  bool needsResource(String? categoryName) =>
      categoryName != null && resourceCategories.contains(categoryName);
  bool needsFile(String? categoryName) =>
      categoryName != null && fileCategories.contains(categoryName);
}

class IssueCatalogsState extends Equatable {
  final bool loaded;
  final List<CatItem> types;
  final List<CatItem> statuses;
  final List<CatItem> categories;
  final List<CatItem> docCategories;
  final List<IssueResource> resources;
  final List<IssueMunicipality> municipalities;

  const IssueCatalogsState({
    this.loaded = false,
    this.types = const [],
    this.statuses = const [],
    this.categories = const [],
    this.docCategories = const [],
    this.resources = const [],
    this.municipalities = const [],
  });

  @override
  List<Object?> get props =>
      [loaded, types, statuses, categories, docCategories, resources, municipalities];
}
