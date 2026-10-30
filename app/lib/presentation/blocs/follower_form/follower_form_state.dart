part of 'follower_form_bloc.dart';

enum FormStatus { invalid, valid, validating, posting }

enum NumberOfPeopleInvited { uno, dos, tres, cuatro, cinco, masdecinco }

class FollowerFormState extends Equatable {
  final bool isValid;
  final bool loading;
  final FormStatus formStatus;
  final bool isInSync;

  final String latitude;
  final String longitude;

  // Datos personales
  final bool isLegalAge;
  final PersonalName names;
  final PersonalName lastName;
  final PersonalName surName;
  final CurpText curp;
  final IneText ine;
  final ExpirationYear expirationYear;
  final String birthday;

  // Contacto
  final Phone cellPhoneNumber;
  final bool supportsWhatsapp;
  final Phone cellPhoneNumberOther;
  final Email email;

  // Domicilio
  final GeneralText residenceMunicipality;
  final GeneralText sectionNumber;
  final GeneralText street;
  final StreetNumber externalNumber;
  final StreetNumber internalNumber;
  final Zipcode zipCode;
  final GeneralText neighborhood;
  final String sectionNumberIne;
  final Sex catSex;

  // Encuesta
  final bool supportsMovement;
  final bool committeeParticipation;
  final bool pollinStationRepresentativeParticipation;
  final bool invitingOthersToParticipate;
  final NumberOfPeopleInvited numberOfPeopleInvited;

  final Uint8List? photo;
  final String ineData;

  // Dónde queda (msvc-structure capture-targets)
  /// El puesto elegido; null si no hay puestos guardados (el servidor usa el
  /// de quien captura).
  final int? positionId;
  /// "Movilizador · S-0233A1-11".
  final String targetLabel;
  /// El puesto es de una sección: el convencido sólo puede ser de ésa.
  final bool sectionLocked;

  const FollowerFormState({
    this.positionId,
    this.targetLabel = '',
    this.sectionLocked = false,
    this.latitude = '',
    this.longitude = '',
    this.names = const PersonalName.pure(),
    this.lastName = const PersonalName.pure(),
    this.surName = const PersonalName.pure(),
    this.curp = const CurpText.pure(isRequired: false),
    this.ine = const IneText.pure(isRequired: false),
    this.birthday = '01-11-2023',
    this.cellPhoneNumber = const Phone.pure(isRequired: true),
    this.cellPhoneNumberOther = const Phone.pure(isRequired: false),
    this.sectionNumberIne = '',
    this.isInSync = false,
    this.residenceMunicipality = const GeneralText.pure(isRequired: true),
    this.sectionNumber = const GeneralText.pure(isRequired: true),
    this.street = const GeneralText.pure(isRequired: true),
    this.externalNumber = const StreetNumber.pure(isRequired: true),
    this.internalNumber = const StreetNumber.pure(isRequired: false),
    this.zipCode = const Zipcode.pure(),
    this.neighborhood = const GeneralText.pure(isRequired: false),
    this.supportsWhatsapp = false,
    this.supportsMovement = false,
    this.committeeParticipation = false,
    this.pollinStationRepresentativeParticipation = false,
    this.invitingOthersToParticipate = false,
    this.numberOfPeopleInvited = NumberOfPeopleInvited.uno,
    this.isValid = false,
    this.loading = false,
    this.formStatus = FormStatus.invalid,
    this.photo,
    this.ineData = '',
    this.email = const Email.pure(isRequired: false),
    this.expirationYear = const ExpirationYear.pure(isRequired: true),
    this.isLegalAge = true,
    this.catSex = const Sex(id: 1, nombre: 'Hombre'),
  });

  FollowerFormState copyWith({
    int? positionId,
    String? targetLabel,
    bool? sectionLocked,
    bool? isValid,
    bool? loading,
    FormStatus? formStatus,
    String? latitude,
    String? longitude,
    Uint8List? photo,
    String? ineData,
    GeneralText? residenceMunicipality,
    GeneralText? sectionNumber,
    GeneralText? street,
    StreetNumber? externalNumber,
    StreetNumber? internalNumber,
    Zipcode? zipCode,
    GeneralText? neighborhood,
    PersonalName? names,
    PersonalName? lastName,
    PersonalName? surName,
    Phone? cellPhoneNumber,
    Phone? cellPhoneNumberOther,
    CurpText? curp,
    IneText? ine,
    String? birthday,
    bool? supportsMovement,
    bool? committeeParticipation,
    bool? pollinStationRepresentativeParticipation,
    bool? invitingOthersToParticipate,
    NumberOfPeopleInvited? numberOfPeopleInvited,
    bool? supportsWhatsapp,
    Email? email,
    ExpirationYear? expirationYear,
    bool? isLegalAge,
    String? sectionNumberIne,
    Sex? catSex,
    bool? isInSync,
  }) {
    return FollowerFormState(
      isValid: isValid ?? this.isValid,
      loading: loading ?? this.loading,
      formStatus: formStatus ?? this.formStatus,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      photo: photo ?? this.photo,
      residenceMunicipality: residenceMunicipality ?? this.residenceMunicipality,
      sectionNumber: sectionNumber ?? this.sectionNumber,
      street: street ?? this.street,
      externalNumber: externalNumber ?? this.externalNumber,
      internalNumber: internalNumber ?? this.internalNumber,
      zipCode: zipCode ?? this.zipCode,
      ineData: ineData ?? this.ineData,
      names: names ?? this.names,
      lastName: lastName ?? this.lastName,
      surName: surName ?? this.surName,
      cellPhoneNumber: cellPhoneNumber ?? this.cellPhoneNumber,
      cellPhoneNumberOther: cellPhoneNumberOther ?? this.cellPhoneNumberOther,
      curp: curp ?? this.curp,
      ine: ine ?? this.ine,
      birthday: birthday ?? this.birthday,
      supportsMovement: supportsMovement ?? this.supportsMovement,
      committeeParticipation:
          committeeParticipation ?? this.committeeParticipation,
      pollinStationRepresentativeParticipation:
          pollinStationRepresentativeParticipation ??
              this.pollinStationRepresentativeParticipation,
      invitingOthersToParticipate:
          invitingOthersToParticipate ?? this.invitingOthersToParticipate,
      numberOfPeopleInvited:
          numberOfPeopleInvited ?? this.numberOfPeopleInvited,
      supportsWhatsapp: supportsWhatsapp ?? this.supportsWhatsapp,
      email: email ?? this.email,
      neighborhood: neighborhood ?? this.neighborhood,
      expirationYear: expirationYear ?? this.expirationYear,
      isLegalAge: isLegalAge ?? this.isLegalAge,
      sectionNumberIne: sectionNumberIne ?? this.sectionNumberIne,
      catSex: catSex ?? this.catSex,
      isInSync: isInSync ?? this.isInSync,
      positionId: positionId ?? this.positionId,
      targetLabel: targetLabel ?? this.targetLabel,
      sectionLocked: sectionLocked ?? this.sectionLocked,
    );
  }

  @override
  List<Object?> get props => [
        isValid,
        loading,
        formStatus,
        latitude,
        longitude,
        photo,
        residenceMunicipality,
        sectionNumber,
        street,
        externalNumber,
        internalNumber,
        zipCode,
        neighborhood,
        ineData,
        names,
        lastName,
        surName,
        cellPhoneNumber,
        cellPhoneNumberOther,
        curp,
        ine,
        birthday,
        supportsMovement,
        committeeParticipation,
        pollinStationRepresentativeParticipation,
        invitingOthersToParticipate,
        numberOfPeopleInvited,
        supportsWhatsapp,
        email,
        expirationYear,
        isLegalAge,
        sectionNumberIne,
        catSex,
        isInSync,
        positionId,
        targetLabel,
        sectionLocked,
      ];
}

final class FollowerFormInitial extends FollowerFormState {}
