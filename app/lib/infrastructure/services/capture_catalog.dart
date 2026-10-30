import 'package:csv/csv.dart';
import 'package:drift/drift.dart';
import 'package:flutter/services.dart';

import '../../domain/entities/user_settings/user_settings.dart';
import '../datasources/user_settings/local_storage_user_settings_datasource_impl.dart';

import '../datasources/storage/app_database.dart';
import '../datasources/structure/structure_capture_datasource.dart';
import 'auth_session.dart';

/// Lo que la captura necesita sin red: los puestos donde se puede dejar al
/// convencido (capture-targets) y el polígono de cada sección. Se refresca
/// cuando hay sesión y red; sin red se usa lo guardado.
class CaptureCatalog {
  final AppDatabase _db = appDatabase;
  final StructureCaptureDatasource _remote = StructureCaptureDatasource();

  /// Un polígono se vuelve a pedir pasado este tiempo (casi nunca cambian).
  static const _geometryMaxAge = Duration(days: 7);

  /// Los puestos guardados: primero los propios.
  Future<List<CaptureTargetRow>> targets() {
    return (_db.select(_db.captureTargets)
          ..orderBy([
            (t) => OrderingTerm(expression: t.own, mode: OrderingMode.desc),
            (t) => OrderingTerm(expression: t.label),
          ]))
        .get();
  }

  Future<String?> geometry(int sectionGeoId) async {
    final row = await (_db.select(_db.sectionGeometries)
          ..where((t) => t.sectionGeoId.equals(sectionGeoId)))
        .getSingleOrNull();
    return row?.geoJson;
  }

  /// Olvida los puestos y polígonos de la cuenta que cerró sesión.
  Future<void> clear() async {
    await _db.delete(_db.captureTargets).go();
    await _db.delete(_db.sectionGeometries).go();
  }

  /// Baja los puestos y los polígonos que falten. Devuelve cuántos puestos
  /// hay; lanza [OfflineException] o [SessionExpiredException].
  Future<int> refresh() async {
    final token = await AuthSession.validToken();
    final List<Map<String, dynamic>> remote;
    try {
      remote = await _remote.captureTargets(token);
    } on Exception catch (e) {
      throw OfflineException('No se pudieron leer tus puestos: $e');
    }
    final now = DateTime.now();
    await _db.transaction(() async {
      await _db.delete(_db.captureTargets).go();
      for (final t in remote) {
        await _db.into(_db.captureTargets).insert(CaptureTargetsCompanion.insert(
              positionId: Value((t['positionId'] as num).toInt()),
              level: t['level'].toString(),
              label: t['label'].toString(),
              holderName: Value(t['holderName']?.toString()),
              sectionGeoId: Value((t['sectionGeoId'] as num?)?.toInt()),
              municipalityGeoId: Value((t['municipalityGeoId'] as num?)?.toInt()),
              own: Value(t['own'] == true),
              updated: now,
            ));
      }
    });

    // «Mis secciones» salen de sus puestos, no de elegirlas a mano
    await _applyUserSections(remote);

    // los polígonos de las secciones de esos puestos, para validar el GPS offline
    final sections = remote
        .map((t) => (t['sectionGeoId'] as num?)?.toInt())
        .whereType<int>()
        .toSet();
    for (final id in sections) {
      final current = await (_db.select(_db.sectionGeometries)
            ..where((t) => t.sectionGeoId.equals(id)))
          .getSingleOrNull();
      if (current != null && now.difference(current.updated) < _geometryMaxAge) {
        continue;
      }
      try {
        final geo = await _remote.sectionGeometry(token, id);
        if (geo != null) {
          await _db.into(_db.sectionGeometries).insertOnConflictUpdate(
              SectionGeometriesCompanion.insert(
                  sectionGeoId: Value(id), geoJson: geo, updated: now));
        }
      } catch (_) {
        // sin polígono se captura igual: el servidor valida al sincronizar
      }
    }
    return remote.length;
  }

  /// Deja seleccionados en `user_settings` el municipio y las secciones de los
  /// puestos de quien inició sesión: el movilizador, su sección; el promotor,
  /// la suya y las de su rama; el referente (sin sección propia), todas las de
  /// su municipio. Los nombres salen de los CSV de la app (mismos ids que el
  /// catálogo de geography).
  Future<void> _applyUserSections(List<Map<String, dynamic>> targets) async {
    if (targets.isEmpty) {
      return;
    }
    final own = targets.firstWhere((t) => t['own'] == true, orElse: () => targets.first);
    final munId = (own['municipalityGeoId'] as num?)?.toInt();
    if (munId == null) {
      return;
    }

    final List<dynamic> munCsv =
        csv.decode(await rootBundle.loadString('assets/geography/geo_municipalities.csv'));
    final munName = munCsv
        .skip(1)
        .where((r) => r[munCsv[0].indexOf('id')].toString() == '$munId')
        .map((r) => r[munCsv[0].indexOf('mun')].toString())
        .firstOrNull;

    final List<dynamic> secCsv =
        csv.decode(await rootBundle.loadString('assets/geography/geo_secciones.csv'));
    final idIdx = secCsv[0].indexOf('id');
    final secIdx = secCsv[0].indexOf('seccion');
    final munIdx = secCsv[0].indexOf('municipio_id');
    final names = {
      for (final r in secCsv.skip(1))
        int.tryParse(r[idIdx].toString()):
            (r[secIdx].toString().padLeft(4, '0'), r[munIdx].toString()),
    };

    // las propias primero, luego las de la rama, sin repetir
    final ordered = [
      ...targets.where((t) => t['own'] == true),
      ...targets.where((t) => t['own'] != true),
    ];
    final ids = <int>{
      for (final t in ordered)
        if ((t['sectionGeoId'] as num?) != null) (t['sectionGeoId'] as num).toInt(),
    };
    if (ids.isEmpty) {
      // referente: todas las secciones de su municipio
      ids.addAll(names.entries
          .where((e) => e.key != null && e.value.$2 == '$munId')
          .map((e) => e.key!));
    }
    final sections = [
      for (final id in ids)
        SectionsSelected(idSec: id, nomSec: names[id]?.$1 ?? '$id'),
    ];
    await LocalStorageUserSettingsDatasourceImpl().saveSections(
        sections, MunicipalitySelected(idMun: munId, nomMun: munName ?? '$munId'));
  }
}
