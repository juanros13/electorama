part of 'visits_people_form_bloc.dart';

enum FormStatus { invalid, valid, validating, posting }

enum Sex { H, M }

class VisitPeopleFormState extends Equatable {
  final bool isValid;
  final FormStatus formStatus;
  final GeneralText street;
  final StreetNumber externalNumber;
  final String latitude;
  final String longitude;
  final bool openTheDoor;
  final Age? age;
  final Sex sex;
  final String hasSympathy;
  final PersonalName? names;
  final PersonalName? lastName;
  final PersonalName? surName;
  final bool acceptInformation;
  final Phone? phone;
  final bool completeSubmit;
  final bool syncVisit;


  const VisitPeopleFormState({
    this.isValid = false,
    this.formStatus = FormStatus.invalid,
    this.street = const GeneralText.pure(),
    this.externalNumber = const StreetNumber.pure(),
    this.latitude = '',
    this.longitude = '',
    this.openTheDoor = false,
    this.age,
    this.sex = Sex.H,
    this.hasSympathy = 'Si',
    this.names = const PersonalName.pure(),
    this.lastName = const PersonalName.pure(),
    this.surName = const PersonalName.pure(),
    this.acceptInformation = false,
    this.phone = const Phone.pure(),
    this.completeSubmit = false,
    this.syncVisit = false,
  });

  VisitPeopleFormState copyWith({
    bool? isValid,
    FormStatus? formStatus,
    GeneralText? street,
    StreetNumber? externalNumber,
    String? latitude,
    String? longitude,
    bool? openTheDoor,
    Age? age,
    Sex? sex,
    String? hasSympathy,
    PersonalName? names,
    PersonalName? lastName,
    PersonalName? surName,
    bool? acceptInformation,
    Phone? phone,
    bool? completeSubmit,
    bool? syncVisit, 
  }) =>
    VisitPeopleFormState(
        isValid: isValid ?? this.isValid,
        formStatus: formStatus ?? this.formStatus,
        street: street ?? this.street,
        externalNumber: externalNumber ?? this.externalNumber,
        latitude: latitude ?? this.latitude,
        longitude: longitude ?? this.longitude,
        openTheDoor: openTheDoor ?? this.openTheDoor,
        age: age ?? this.age,
        sex: sex ?? this.sex,
        hasSympathy: hasSympathy ?? this.hasSympathy,
        names : names ?? this.names,
        lastName: lastName ?? this.lastName,
        surName:  surName ?? this.surName,
        acceptInformation:  acceptInformation ?? this.acceptInformation,
        phone: phone ?? this.phone,
        completeSubmit:  completeSubmit ?? this.completeSubmit,
        syncVisit: syncVisit ?? this.syncVisit,
    );

  @override
  List<Object?> get props => [
    isValid,
    formStatus,
    street,
    externalNumber,
    latitude,
    longitude,
    openTheDoor,
    sex,
    age,
    hasSympathy,
    names,
    lastName,
    surName,
    acceptInformation,
    phone,
    completeSubmit,
    syncVisit
  ];
}

class VisitPeopleInitial extends VisitPeopleFormState {}
