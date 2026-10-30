import 'dart:convert';

import 'package:drift/drift.dart';
import 'package:electorama_app/domain/datasources/user_settings/local_storage_user_settings_datasource.dart';
import 'package:electorama_app/domain/entities/user_settings/user_settings.dart';
import 'package:electorama_app/infrastructure/datasources/storage/app_database.dart';

class LocalStorageUserSettingsDatasourceImpl
    implements LocalStorageUserSettingsDatasource {
  final AppDatabase _db = appDatabase;

  UserSettings _toEntity(UserSettingsRow row) {
    final List<dynamic> raw = jsonDecode(row.sectionsSelected) as List<dynamic>;
    final sections = raw
        .map((e) => SectionsSelected(idSec: e['idSec'], nomSec: e['nomSec']))
        .toList();
    return UserSettings(
      id: row.id,
      municipality: MunicipalitySelected(
          idMun: row.municipalityId, nomMun: row.municipalityName),
      sectionsSelected: sections,
    );
  }

  String _encode(List<SectionsSelected> sections) => jsonEncode(
      sections.map((s) => {'idSec': s.idSec, 'nomSec': s.nomSec}).toList());

  @override
  Future<UserSettings> saveMunicipality(UserSettings settings) async {
    if (settings.id == null) {
      final id = await _db.into(_db.userSettingsTable).insert(
            UserSettingsTableCompanion.insert(
              municipalityId: Value(settings.municipality.idMun),
              municipalityName: Value(settings.municipality.nomMun),
              sectionsSelected: Value(_encode(settings.sectionsSelected)),
            ),
          );
      settings.id = id;
    } else {
      await (_db.update(_db.userSettingsTable)
            ..where((t) => t.id.equals(settings.id!)))
          .write(UserSettingsTableCompanion(
        municipalityId: Value(settings.municipality.idMun),
        municipalityName: Value(settings.municipality.nomMun),
      ));
    }
    return settings;
  }

  @override
  Future<UserSettings> saveSections(List<SectionsSelected> sections,
      MunicipalitySelected municipalitySelected) async {
    final row =
        await (_db.select(_db.userSettingsTable)..limit(1)).getSingleOrNull();
    if (row == null) {
      final id = await _db.into(_db.userSettingsTable).insert(
            UserSettingsTableCompanion.insert(
              municipalityId: Value(municipalitySelected.idMun),
              municipalityName: Value(municipalitySelected.nomMun),
              sectionsSelected: Value(_encode(sections)),
            ),
          );
      return UserSettings(
          id: id,
          municipality: municipalitySelected,
          sectionsSelected: sections);
    }
    await (_db.update(_db.userSettingsTable)..where((t) => t.id.equals(row.id)))
        .write(UserSettingsTableCompanion(
      municipalityId: Value(municipalitySelected.idMun),
      municipalityName: Value(municipalitySelected.nomMun),
      sectionsSelected: Value(_encode(sections)),
    ));
    return UserSettings(
        id: row.id,
        municipality: municipalitySelected,
        sectionsSelected: sections);
  }

  @override
  Future<List<UserSettings>> getAllUserSettings() async {
    final rows = await _db.select(_db.userSettingsTable).get();
    return rows.map(_toEntity).toList();
  }

  @override
  Future<UserSettings> getUserSettingsById(int isarId) async {
    final row = await (_db.select(_db.userSettingsTable)
          ..where((t) => t.id.equals(isarId)))
        .getSingle();
    return _toEntity(row);
  }

  @override
  Future<bool> deleteUserSettingsById(int isarId) async {
    final count = await (_db.delete(_db.userSettingsTable)
          ..where((t) => t.id.equals(isarId)))
        .go();
    return count > 0;
  }

  @override
  Future<List<int>> deleteAllUserSettings() async {
    await _db.delete(_db.userSettingsTable).go();
    return [];
  }

  @override
  Future<UserSettings> findUserSettings() async {
    final row =
        await (_db.select(_db.userSettingsTable)..limit(1)).getSingle();
    return _toEntity(row);
  }
}
