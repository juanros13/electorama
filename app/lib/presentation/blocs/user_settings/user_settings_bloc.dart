import 'package:csv/csv.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/domain/entities/user_settings/user_settings.dart';
import 'package:electorama_app/domain/repositories/user_settings/local_storage_user_settings_repository.dart';
import 'package:electorama_app/infrastructure/datasources/user_settings/local_storage_user_settings_datasource_impl.dart';
import 'package:electorama_app/infrastructure/repositories/user_settings/local_storage_user_settings_repository_impl.dart';

part 'user_settings_event.dart';
part 'user_settings_state.dart';

class UserSettingsBloc extends Bloc<UserSettingsEvent, UserSettingsState> {
  LocalStorageUserSettingsRepository localStorageRepository =
      LocalStorageUserSettingsRepositoryImpl(
          LocalStorageUserSettingsDatasourceImpl());

  UserSettingsBloc() : super(UserSettingsInitial()) {
    on<InitSelectMunicipality>(_initSelectMunicipality);
    on<LoadAllSettings>(_loadAllSettings);
    on<SaveMunicipality>(_saveMunicipality);
    on<LoadSections>(_loadSections);
    on<LoadSettings>(_loadSettings);
    on<AddSectionsSelected>(_addSectionsSelected);
    on<RemoveSectionsSelected>(_removeSectionsSelected);
    on<SelectMunicipality>(_selectMunicipality);
    on<AddSectionSelected>(_addSectionSelected);
  }

  void _initSelectMunicipality(
      InitSelectMunicipality event, Emitter<UserSettingsState> emit) async {
    emit(state.copyWith(loading: true));
    final settings = UserSettings(
        municipality:
            MunicipalitySelected(idMun: 1983, nomMun: 'JALPA DE MENDEZ'));
    await localStorageRepository.saveMunicipality(settings);
    emit(state.copyWith(loading: false));
  }

  void _loadAllSettings(
      LoadAllSettings event, Emitter<UserSettingsState> emit) async {
    emit(state.copyWith(loading: true));
    final settings = await localStorageRepository.getAllUserSettings();
    if (settings.isEmpty) {
      final created = UserSettings(
          municipality: MunicipalitySelected(idMun: 1977, nomMun: 'CENTRO'));
      await localStorageRepository.saveMunicipality(created);
    }
    emit(state.copyWith(loading: false, listSettings: settings));
  }

  void _saveMunicipality(
      SaveMunicipality event, Emitter<UserSettingsState> emit) async {
    emit(state.copyWith(loading: true));
    final userSettings = await localStorageRepository.findUserSettings();
    userSettings.municipality.idMun = event.settings.municipality.idMun;
    userSettings.municipality.nomMun = event.settings.municipality.nomMun;
    await localStorageRepository.saveMunicipality(userSettings);
    emit(state.copyWith(loading: false));
  }

  void _addSectionSelected(
      AddSectionSelected event, Emitter<UserSettingsState> emit) async {
    final List<SectionsSelected> list = [];
    for (var secc in state.sectionListSelected) {
      list.add(SectionsSelected(idSec: secc.idSec, nomSec: secc.nomSec));
    }
    final municipalitySelected = MunicipalitySelected(
        idMun: state.municipalitySelected['id'],
        nomMun: state.municipalitySelected['name']);
    await localStorageRepository.saveSections(list, municipalitySelected);
  }

  void _selectMunicipality(
      SelectMunicipality event, Emitter<UserSettingsState> emit) {
    emit(state.copyWith(
        municipalitySelected: event.value, sectionListSelected: []));
  }

  void _addSectionsSelected(
      AddSectionsSelected event, Emitter<UserSettingsState> emit) {
    final sections = state.sectionListSelected.map((e) => e).toList();
    sections.add(event.value);
    emit(state.copyWith(sectionListSelected: sections));
  }

  void _removeSectionsSelected(
      RemoveSectionsSelected event, Emitter<UserSettingsState> emit) {
    final sections = state.sectionListSelected.map((e) => e).toList();
    sections.removeWhere((secc) => secc.idSec == event.value.idSec);
    emit(state.copyWith(sectionListSelected: sections));
  }

  void _loadSettings(LoadSettings event, Emitter<UserSettingsState> emit) async {
    final listSettings = await localStorageRepository.getAllUserSettings();
    if (listSettings.isNotEmpty) {
      final settings = listSettings[0];
      emit(state.copyWith(
          municipalitySelected: {
            'id': settings.municipality.idMun!,
            'name': settings.municipality.nomMun
          },
          sectionListSelected: settings.sectionsSelected));
    }
  }

  void _loadSections(LoadSections event, Emitter<UserSettingsState> emit) async {
    final resultMun =
        await rootBundle.loadString('assets/geography/geo_municipalities.csv');
    final List<dynamic> csvTableMun =
        csv.decode(resultMun);
    final idIndex = csvTableMun[0].indexOf('id');
    final munIndex = csvTableMun[0].indexOf('mun');
    final List<dynamic> municipalitiesList = [];
    for (var i = 1; i < csvTableMun.length; i++) {
      municipalitiesList.add({
        'id': csvTableMun[i][idIndex].toString(),
        'nomNum': csvTableMun[i][munIndex],
      });
    }

    final result =
        await rootBundle.loadString('assets/geography/geo_secciones.csv');
    final List<dynamic> csvTable = csv.decode(result);
    final idx = csvTable[0].indexOf('id');
    final seccionIdx = csvTable[0].indexOf('seccion');
    final municipioIdx = csvTable[0].indexOf('municipio_id');

    final List<SectionsSelected> sectionsList = [];
    final selectedMun = state.municipalitySelected['id'].toString();
    for (var i = 1; i < csvTable.length; i++) {
      // csv 8 devuelve los valores como String; parseamos lo necesario.
      final municipioId = csvTable[i][municipioIdx].toString();
      if (selectedMun == municipioId) {
        final seccionId = int.tryParse(csvTable[i][idx].toString());
        final seccion = csvTable[i][seccionIdx].toString().padLeft(4, '0');
        sectionsList.add(SectionsSelected(idSec: seccionId, nomSec: seccion));
      }
    }
    emit(state.copyWith(
      sectionList: sectionsList,
      municipalitiesList: municipalitiesList,
    ));
  }
}
