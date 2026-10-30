part of 'coordinator_zone_form_bloc.dart';

class CordinatorZoneFormState extends Equatable {
  
  final bool isValid;
  final FormStatus formStatus;
  final GeneralDropdown sectionSelected;
  final GeneralDropdown municipalitySelected;
  final GeneralDropdown localitySelected;

  const CordinatorZoneFormState({
    this.sectionSelected = const GeneralDropdown.pure(), 
    this.municipalitySelected = const GeneralDropdown.pure(), 
    this.localitySelected = const GeneralDropdown.pure(),
    this.isValid = false,
    this.formStatus = FormStatus.invalid,
  });

  CordinatorZoneFormState copyWith({
    bool? isValid,
    FormStatus? formStatus,
    GeneralDropdown? sectionSelected,
    GeneralDropdown? municipalitySelected,
    GeneralDropdown? localitySelected
  }) => CordinatorZoneFormState(
    isValid: isValid ?? this.isValid,
    formStatus: formStatus ?? this.formStatus,
    sectionSelected: sectionSelected ?? this.sectionSelected,
    municipalitySelected: municipalitySelected ?? this.municipalitySelected,
    localitySelected: localitySelected ?? this.localitySelected,
  );
  
  @override
  List<Object> get props => [sectionSelected, municipalitySelected, localitySelected, isValid, formStatus];
}

class CordinatorZoneFormInitial extends CordinatorZoneFormState {}
