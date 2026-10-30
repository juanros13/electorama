part of 'user_settings_bloc.dart';

sealed class UserSettingsEvent extends Equatable {
  const UserSettingsEvent();

  @override
  List<Object> get props => [];
}

class InitSelectMunicipality extends UserSettingsEvent {
  const InitSelectMunicipality();
}

class FindFirstMunicipality extends UserSettingsEvent {
  const FindFirstMunicipality();
}

class LoadAllSettings extends UserSettingsEvent {
  const LoadAllSettings();
}

class SaveMunicipality extends UserSettingsEvent {
  final UserSettings settings;
  const SaveMunicipality({required this.settings});
}

class LoadSections extends UserSettingsEvent {
  const LoadSections();
}

class LoadSettings extends UserSettingsEvent {
  const LoadSettings();
}

class SelectMunicipality extends UserSettingsEvent {
  final dynamic value;
  const SelectMunicipality(this.value);
}

class AddSectionsSelected extends UserSettingsEvent {
  final SectionsSelected value;
  const AddSectionsSelected(this.value);
}

class RemoveSectionsSelected extends UserSettingsEvent {
  final SectionsSelected value;
  const RemoveSectionsSelected(this.value);
}

class AddSectionSelected extends UserSettingsEvent {
  const AddSectionSelected();
}
