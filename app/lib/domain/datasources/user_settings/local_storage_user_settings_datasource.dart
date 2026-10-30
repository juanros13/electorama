import 'package:electorama_app/domain/entities/user_settings/user_settings.dart';

abstract class LocalStorageUserSettingsDatasource {
  Future<UserSettings> saveMunicipality(UserSettings settings);
  Future<UserSettings> saveSections(
      List<SectionsSelected> sections, MunicipalitySelected municipalitySelected);
  Future<UserSettings> getUserSettingsById(int isarId);
  Future<List<UserSettings>> getAllUserSettings();
  Future<bool> deleteUserSettingsById(int isarId);
  Future<List<int>> deleteAllUserSettings();
  Future<UserSettings> findUserSettings();
}
