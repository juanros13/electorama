import 'dart:typed_data';

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:formz/formz.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/infrastructure/inputs/curp_text.dart';
import 'package:electorama_app/infrastructure/inputs/expiration_year.dart';
import 'package:electorama_app/infrastructure/inputs/general_text.dart';
import 'package:electorama_app/infrastructure/inputs/ine_text.dart';
import 'package:electorama_app/infrastructure/inputs/email.dart';
import 'package:electorama_app/infrastructure/inputs/personal_name.dart';
import 'package:electorama_app/infrastructure/inputs/phone.dart';
import 'package:electorama_app/infrastructure/inputs/street_number.dart';
import 'package:electorama_app/infrastructure/inputs/zipcode.dart';

part 'follower_form_event.dart';
part 'follower_form_state.dart';

class FollowerFormBloc extends Bloc<FollowerFormEvent, FollowerFormState> {
  FollowerFormBloc() : super(FollowerFormInitial()) {
    on<LatitudeChange>(_latitudeChange);
    on<LongitudeChange>(_longitudeChange);
    on<TakePhoto>(_onTakePhoto);
    on<LoadingFollowers>(_loadingFollowers);
    on<LoadIneData>(_loadIneData);
    on<SupportsWhatsappChange>(_onSupportsWhatsappChange);
    on<SupportsMovementChange>(_onSupportsMovementChange);
    on<CommitteeParticipationChange>(_onCommitteeParticipationChange);
    on<PollinStationRepresentativeParticipationChange>(
        _onPollinStationRepresentativeParticipationChange);
    on<InvitingOthersToParticipateChange>(_onInvitingOthersToParticipateChange);
    on<ResidenceMunicipalityChange>(_onResidenceMunicipalityChange);
    on<SectionNumberChange>(_onSectionNumberChange);
    on<SectionNumberIneChange>(_onSectionNumberIneChange);
    on<StreetChange>(_onStreetChange);
    on<ExternalNumberChange>(_onExternalNumberChange);
    on<InternalNumberChange>(_onInternalNumberChange);
    on<ZipCodeChange>(_onZipCodeChange);
    on<NeighborhoodChange>(_onNeighborhoodChange);
    on<NamesChange>(_namesChange);
    on<LastNameChange>(_lastNameChange);
    on<SurNameChange>(_surNameChange);
    on<PhoneChange>(_phoneChange);
    on<PhoneOtherChange>(_phoneOtherChange);
    on<CurpChange>(_curpChange);
    on<IneChange>(_ineChange);
    on<EmailChange>(_emailChange);
    on<NumberOfPeopleInvitingChange>(_onNumberOfPeopleInvitingChange);
    on<ExpirationYearChange>(_expirationYearChange);
    on<BirthdayChange>(_birthdayChange);
    on<IsLegalAgeChange>(_onIsLegalAgeChange);
    on<CatSexChange>(_onCatSexChange);
    on<ResetForm>(_resetForm);
    on<TargetChange>(_targetChange);
  }

  void _resetForm(ResetForm event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(
      names: const PersonalName.pure(),
      lastName: const PersonalName.pure(),
      surName: const PersonalName.pure(),
      curp: const CurpText.pure(isRequired: false),
      ine: const IneText.pure(isRequired: false),
      cellPhoneNumber: const Phone.pure(isRequired: true),
      cellPhoneNumberOther: const Phone.pure(isRequired: false),
      isInSync: false,
      supportsWhatsapp: false,
      supportsMovement: false,
      committeeParticipation: false,
      pollinStationRepresentativeParticipation: false,
      invitingOthersToParticipate: false,
      numberOfPeopleInvited: NumberOfPeopleInvited.uno,
      loading: false,
      formStatus: FormStatus.invalid,
      email: const Email.pure(isRequired: false),
      expirationYear: const ExpirationYear.pure(isRequired: true),
      isLegalAge: true,
    ));
  }

  void _latitudeChange(LatitudeChange event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(latitude: event.value));
  }

  void _longitudeChange(LongitudeChange event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(longitude: event.value));
  }

  void _expirationYearChange(
      ExpirationYearChange event, Emitter<FollowerFormState> emit) {
    final expirationYear = ExpirationYear.dirty(event.value, isRequired: false);
    emit(state.copyWith(
        expirationYear: expirationYear,
        isValid: validateForm(expirationYearStateFlag: expirationYear)));
  }

  void _birthdayChange(BirthdayChange event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(birthday: event.value));
  }

  void _curpChange(CurpChange event, Emitter<FollowerFormState> emit) {
    final curp = CurpText.dirty(event.value, isRequired: false);
    // la CURP trae fecha de nacimiento y sexo: sin escanear la INE la fecha se
    // quedaba en el default del formulario
    final derived = _fromCurp(event.value);
    emit(state.copyWith(
        curp: curp,
        birthday: derived?.$1,
        catSex: derived?.$2,
        isValid: validateForm(curpStateFlag: curp)));
  }

  /// (fecha 'dd-MM-yyyy', sexo) desde una CURP de 18; null si no se puede.
  static (String, Sex)? _fromCurp(String raw) {
    final curp = raw.trim().toUpperCase();
    final m = RegExp(r'^[A-Z]{4}(\d{2})(\d{2})(\d{2})([HM])').firstMatch(curp);
    if (curp.length != 18 || m == null) {
      return null;
    }
    final yy = int.parse(m[1]!);
    // el dígito 17 distingue siglo; en la práctica, quien vota nació hace 18+ años
    final century = yy <= (DateTime.now().year % 100) - 17 ? 2000 : 1900;
    final sex = m[4] == 'M' ? const Sex(id: 2, nombre: 'Mujer') : const Sex(id: 1, nombre: 'Hombre');
    return ('${m[3]}-${m[2]}-${century + yy}', sex);
  }

  void _targetChange(TargetChange event, Emitter<FollowerFormState> emit) {
    final section = event.sectionGeoId == null
        ? state.sectionNumber
        : GeneralText.dirty(event.sectionGeoId.toString(), isRequired: true);
    emit(state.copyWith(
      positionId: event.positionId,
      targetLabel: event.label,
      sectionLocked: event.sectionGeoId != null,
      sectionNumber: section,
      isValid: validateForm(sectionNumberStateFlag: section),
    ));
  }

  void _ineChange(IneChange event, Emitter<FollowerFormState> emit) {
    final ine = IneText.dirty(event.value, isRequired: false);
    emit(state.copyWith(ine: ine, isValid: validateForm(ineStateFlag: ine)));
  }

  void _emailChange(EmailChange event, Emitter<FollowerFormState> emit) {
    final email = Email.dirty(event.value, isRequired: false);
    emit(state.copyWith(
        email: email, isValid: validateForm(emailStateFlag: email)));
  }

  void _namesChange(NamesChange event, Emitter<FollowerFormState> emit) {
    final names = PersonalName.dirty(event.value);
    emit(state.copyWith(
        names: names, isValid: validateForm(namesStateFlag: names)));
  }

  void _lastNameChange(LastNameChange event, Emitter<FollowerFormState> emit) {
    final lastName = PersonalName.dirty(event.value);
    emit(state.copyWith(
        lastName: lastName, isValid: validateForm(lastNameStateFlag: lastName)));
  }

  void _surNameChange(SurNameChange event, Emitter<FollowerFormState> emit) {
    final surName = PersonalName.dirty(event.value);
    emit(state.copyWith(
        surName: surName, isValid: validateForm(surNameStateFlag: surName)));
  }

  void _phoneChange(PhoneChange event, Emitter<FollowerFormState> emit) {
    final phone = Phone.dirty(event.value, isRequired: true);
    emit(state.copyWith(
        cellPhoneNumber: phone, isValid: validateForm(phoneStateFlag: phone)));
  }

  void _phoneOtherChange(
      PhoneOtherChange event, Emitter<FollowerFormState> emit) {
    final phone = Phone.dirty(event.value, isRequired: false);
    emit(state.copyWith(
        cellPhoneNumberOther: phone,
        isValid: validateForm(phoneOtherStateFlag: phone)));
  }

  void _loadingFollowers(
      LoadingFollowers event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(loading: event.value));
  }

  void _onSupportsMovementChange(
      SupportsMovementChange event, Emitter<FollowerFormState> emit) {
    final supportsMovementSelected = !state.supportsMovement;
    emit(state.copyWith(
        supportsMovement: supportsMovementSelected,
        isValid: validateForm(
            supportsMovementStateFlag: supportsMovementSelected,
            photo: state.photo)));
  }

  void _onIsLegalAgeChange(
      IsLegalAgeChange event, Emitter<FollowerFormState> emit) {
    final isLegalAgeSelected = !state.isLegalAge;
    emit(state.copyWith(
        isLegalAge: isLegalAgeSelected,
        isValid: validateForm(
            isLegarAgeStateFlag: isLegalAgeSelected, photo: state.photo)));
  }

  void _onCatSexChange(CatSexChange event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(catSex: event.value));
  }

  void _onSupportsWhatsappChange(
      SupportsWhatsappChange event, Emitter<FollowerFormState> emit) {
    final selected = !state.supportsWhatsapp;
    emit(state.copyWith(
        supportsWhatsapp: selected,
        isValid: validateForm(
            supportsWhatsappChangeStateFlag: selected, photo: state.photo)));
  }

  void _onCommitteeParticipationChange(
      CommitteeParticipationChange event, Emitter<FollowerFormState> emit) {
    final selected = !state.committeeParticipation;
    emit(state.copyWith(
        committeeParticipation: selected,
        isValid: validateForm(
            committeeParticipationStateFlag: selected, photo: state.photo)));
  }

  void _onPollinStationRepresentativeParticipationChange(
      PollinStationRepresentativeParticipationChange event,
      Emitter<FollowerFormState> emit) {
    final selected = !state.pollinStationRepresentativeParticipation;
    emit(state.copyWith(
        pollinStationRepresentativeParticipation: selected,
        isValid: validateForm(
            pollinStationRepresentativeParticipationStateFlag: selected,
            photo: state.photo)));
  }

  void _onInvitingOthersToParticipateChange(
      InvitingOthersToParticipateChange event,
      Emitter<FollowerFormState> emit) {
    final selected = !state.invitingOthersToParticipate;
    emit(state.copyWith(
        invitingOthersToParticipate: selected,
        isValid: validateForm(
            invitingOthersToParticipate: selected, photo: state.photo)));
  }

  void _onNumberOfPeopleInvitingChange(
      NumberOfPeopleInvitingChange event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(
        numberOfPeopleInvited: event.value,
        isValid: validateForm(
            numberOfPeopleInvitedStateFlag: event.value, photo: state.photo)));
  }

  void _onResidenceMunicipalityChange(
      ResidenceMunicipalityChange event, Emitter<FollowerFormState> emit) {
    final residenceMunicipality =
        GeneralText.dirty(event.value, isRequired: true);
    emit(state.copyWith(
        residenceMunicipality: residenceMunicipality,
        isValid:
            validateForm(residenceMunicipalityFlag: residenceMunicipality)));
  }

  void _onSectionNumberChange(
      SectionNumberChange event, Emitter<FollowerFormState> emit) {
    final sectionNumber = GeneralText.dirty(event.value, isRequired: true);
    emit(state.copyWith(
        sectionNumber: sectionNumber,
        isValid: validateForm(sectionNumberStateFlag: sectionNumber)));
  }

  void _onSectionNumberIneChange(
      SectionNumberIneChange event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(sectionNumberIne: event.value));
  }

  void _onStreetChange(StreetChange event, Emitter<FollowerFormState> emit) {
    final street = GeneralText.dirty(event.value, isRequired: true);
    emit(state.copyWith(
        street: street, isValid: validateForm(streetStateFlag: street)));
  }

  void _onExternalNumberChange(
      ExternalNumberChange event, Emitter<FollowerFormState> emit) {
    final externalNumber = StreetNumber.dirty(event.value, isRequired: true);
    emit(state.copyWith(
        externalNumber: externalNumber,
        isValid: validateForm(externalNumberStateFlag: externalNumber)));
  }

  void _onInternalNumberChange(
      InternalNumberChange event, Emitter<FollowerFormState> emit) {
    final internalNumber = StreetNumber.dirty(event.value, isRequired: false);
    emit(state.copyWith(
        internalNumber: internalNumber,
        isValid: validateForm(internalNumberStateFlag: internalNumber)));
  }

  void _onZipCodeChange(ZipCodeChange event, Emitter<FollowerFormState> emit) {
    final zipcode = Zipcode.dirty(event.value);
    emit(state.copyWith(
        zipCode: zipcode, isValid: validateForm(zipCodeStateFlag: zipcode)));
  }

  void _onNeighborhoodChange(
      NeighborhoodChange event, Emitter<FollowerFormState> emit) {
    final neighborhood = GeneralText.dirty(event.value, isRequired: false);
    emit(state.copyWith(
        neighborhood: neighborhood,
        isValid: validateForm(neighborhoodStateFlag: neighborhood)));
  }

  void _onTakePhoto(TakePhoto event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(
        photo: event.value,
        loading: false,
        isValid: validateForm(photo: event.value)));
  }

  bool validateForm({
    Uint8List? photo,
    bool? supportsMovementStateFlag,
    bool? committeeParticipationStateFlag,
    bool? pollinStationRepresentativeParticipationStateFlag,
    bool? invitingOthersToParticipate,
    NumberOfPeopleInvited? numberOfPeopleInvitedStateFlag,
    bool? supportsWhatsappChangeStateFlag,
    PersonalName? namesStateFlag,
    PersonalName? lastNameStateFlag,
    PersonalName? surNameStateFlag,
    Email? emailStateFlag,
    Phone? phoneStateFlag,
    Phone? phoneOtherStateFlag,
    CurpText? curpStateFlag,
    IneText? ineStateFlag,
    GeneralText? residenceMunicipalityFlag,
    GeneralText? sectionNumberStateFlag,
    GeneralText? streetStateFlag,
    StreetNumber? externalNumberStateFlag,
    StreetNumber? internalNumberStateFlag,
    Zipcode? zipCodeStateFlag,
    GeneralText? neighborhoodStateFlag,
    ExpirationYear? expirationYearStateFlag,
    bool? isLegarAgeStateFlag,
  }) {
    supportsMovementStateFlag ??= state.supportsMovement;
    residenceMunicipalityFlag ??= state.residenceMunicipality;
    sectionNumberStateFlag ??= state.sectionNumber;
    streetStateFlag ??= state.street;
    externalNumberStateFlag ??= state.externalNumber;
    internalNumberStateFlag ??= state.internalNumber;
    zipCodeStateFlag ??= state.zipCode;
    neighborhoodStateFlag ??= state.neighborhood;
    namesStateFlag ??= state.names;
    lastNameStateFlag ??= state.lastName;
    surNameStateFlag ??= state.surName;
    phoneStateFlag ??= state.cellPhoneNumber;
    curpStateFlag ??= state.curp;
    ineStateFlag ??= state.ine;
    emailStateFlag ??= state.email;
    phoneOtherStateFlag ??= state.cellPhoneNumberOther;
    expirationYearStateFlag ??= state.expirationYear;

    if (!supportsMovementStateFlag) {
      return Formz.validate([
        externalNumberStateFlag,
        sectionNumberStateFlag,
        streetStateFlag,
        zipCodeStateFlag,
        neighborhoodStateFlag,
      ]);
    } else {
      final List<FormzInput> base = [
        externalNumberStateFlag,
        streetStateFlag,
        zipCodeStateFlag,
        neighborhoodStateFlag,
        namesStateFlag,
        lastNameStateFlag,
        surNameStateFlag,
        phoneStateFlag,
        curpStateFlag,
        ineStateFlag,
        emailStateFlag,
        phoneOtherStateFlag,
      ];
      if (ineStateFlag.value.isNotEmpty) {
        base.add(expirationYearStateFlag);
      }
      return Formz.validate(base);
    }
  }

  void _loadIneData(LoadIneData event, Emitter<FollowerFormState> emit) {
    emit(state.copyWith(ineData: event.value));
  }
}
