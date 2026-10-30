part of 'user_settings_bloc.dart';

class UserSettingsState extends Equatable {
  final bool loading;
  final UserSettings? settings;
  final List<UserSettings>? listSettings;
  final List<SectionsSelected> sectionList;
  final List<SectionsSelected> sectionListSelected;
  final List<dynamic> municipalitiesList;
  final dynamic municipalitySelected;

  const UserSettingsState({
    this.loading = false,
    this.settings,
    this.listSettings,
    this.sectionList = const [],
    this.municipalitiesList = const [],
    this.sectionListSelected = const [],
    this.municipalitySelected = const {'id': 1977, 'name': 'CENTRO'},
  });

  UserSettingsState copyWith({
    bool? loading,
    UserSettings? settings,
    List<UserSettings>? listSettings,
    List<SectionsSelected>? sectionList,
    List<dynamic>? municipalitiesList,
    List<SectionsSelected>? sectionListSelected,
    dynamic municipalitySelected,
  }) =>
      UserSettingsState(
        loading: loading ?? this.loading,
        settings: settings ?? this.settings,
        listSettings: listSettings ?? this.listSettings,
        sectionList: sectionList ?? this.sectionList,
        municipalitiesList: municipalitiesList ?? this.municipalitiesList,
        sectionListSelected: sectionListSelected ?? this.sectionListSelected,
        municipalitySelected: municipalitySelected ?? this.municipalitySelected,
      );

  @override
  List<Object?> get props => [
        loading,
        settings,
        listSettings,
        sectionList,
        municipalitiesList,
        sectionListSelected,
        municipalitySelected,
      ];
}

final class UserSettingsInitial extends UserSettingsState {}
