import 'package:electorama_app/domain/datasources/user_settings/local_storage_user_settings_datasource.dart';
import 'package:electorama_app/domain/entities/user_settings/user_settings.dart';
import 'package:electorama_app/domain/repositories/user_settings/local_storage_user_settings_repository.dart';

class LocalStorageUserSettingsRepositoryImpl
    extends LocalStorageUserSettingsRepository {
  final LocalStorageUserSettingsDatasource datasource;

  LocalStorageUserSettingsRepositoryImpl(this.datasource);

  @override
  Future<UserSettings> saveMunicipality(UserSettings settings) =>
      datasource.saveMunicipality(settings);

  @override
  Future<UserSettings> saveSections(List<SectionsSelected> sections,
          MunicipalitySelected municipalitySelected) =>
      datasource.saveSections(sections, municipalitySelected);

  @override
  Future<bool> deleteUserSettingsById(int isarId) =>
      datasource.deleteUserSettingsById(isarId);

  @override
  Future<List<UserSettings>> getAllUserSettings() =>
      datasource.getAllUserSettings();

  @override
  Future<UserSettings> getUserSettingsById(int isarId) =>
      datasource.getUserSettingsById(isarId);

  @override
  Future<List<int>> deleteAllUserSettings() =>
      datasource.deleteAllUserSettings();

  @override
  Future<UserSettings> findUserSettings() => datasource.findUserSettings();
}
