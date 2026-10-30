import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:formz/formz.dart';
import 'package:electorama_app/infrastructure/inputs/general_dropdown.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people_form/visits_people_form_bloc.dart';

part 'coordinator_zone_form_event.dart';
part 'coordinator_zone_form_state.dart';

class CordinatorZoneFormBloc extends Bloc<CordinatorZoneFormEvent, CordinatorZoneFormState> {
  CordinatorZoneFormBloc() : super(CordinatorZoneFormInitial()) {
    on<SectionChange>(_onSectionChange);
    on<MunicipalityChange>(_onMunicipalityChange);
    on<LocalityChange>(_onLocalityChange);
  }
  void _onSectionChange(SectionChange event, Emitter<CordinatorZoneFormState> emit){
    final sectionSelected = GeneralDropdown.dirty(event.value);
    emit(state.copyWith(
      sectionSelected: sectionSelected,
      localitySelected: const GeneralDropdown.dirty(""),
      isValid: false
    ));
  }
  void _onMunicipalityChange(MunicipalityChange event, Emitter<CordinatorZoneFormState> emit){
    final municipalitySelected = GeneralDropdown.dirty(event.value);
    emit(state.copyWith(
      municipalitySelected: municipalitySelected,
      isValid: Formz.validate([state.sectionSelected, state.localitySelected])
    ));
  }
  void _onLocalityChange(LocalityChange event, Emitter<CordinatorZoneFormState> emit){
    final localitySelected = GeneralDropdown.dirty(event.value);
    emit(state.copyWith(
      localitySelected: localitySelected,
      isValid: Formz.validate([localitySelected, state.sectionSelected])
    ));
  }
}

