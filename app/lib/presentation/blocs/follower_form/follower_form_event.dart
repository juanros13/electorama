part of 'follower_form_bloc.dart';

sealed class FollowerFormEvent extends Equatable {
  const FollowerFormEvent();

  @override
  List<Object> get props => [];
}

class SubmitForm extends FollowerFormEvent {
  const SubmitForm();
}

class LatitudeChange extends FollowerFormEvent {
  final String value;
  const LatitudeChange(this.value);
}

class LongitudeChange extends FollowerFormEvent {
  final String value;
  const LongitudeChange(this.value);
}

class TakePhoto extends FollowerFormEvent {
  final Uint8List value;
  const TakePhoto(this.value);
}

class LoadingFollowers extends FollowerFormEvent {
  final bool value;
  const LoadingFollowers(this.value);
}

class SupportsMovementChange extends FollowerFormEvent {
  final bool value;
  const SupportsMovementChange(this.value);
}

class CommitteeParticipationChange extends FollowerFormEvent {
  final bool value;
  const CommitteeParticipationChange(this.value);
}

class SupportsWhatsappChange extends FollowerFormEvent {
  final bool value;
  const SupportsWhatsappChange(this.value);
}

class PollinStationRepresentativeParticipationChange extends FollowerFormEvent {
  final bool value;
  const PollinStationRepresentativeParticipationChange(this.value);
}

class InvitingOthersToParticipateChange extends FollowerFormEvent {
  final bool value;
  const InvitingOthersToParticipateChange(this.value);
}

class IsLegalAgeChange extends FollowerFormEvent {
  final bool value;
  const IsLegalAgeChange(this.value);
}

class NumberOfPeopleInvitingChange extends FollowerFormEvent {
  final NumberOfPeopleInvited value;
  const NumberOfPeopleInvitingChange(this.value);
}

class StreetChange extends FollowerFormEvent {
  final String value;
  const StreetChange(this.value);
}

class SectionNumberChange extends FollowerFormEvent {
  final String value;
  const SectionNumberChange(this.value);
}

class ResidenceMunicipalityChange extends FollowerFormEvent {
  final String value;
  const ResidenceMunicipalityChange(this.value);
}

class CatSexChange extends FollowerFormEvent {
  final Sex value;
  const CatSexChange(this.value);
}

class SectionNumberIneChange extends FollowerFormEvent {
  final String value;
  const SectionNumberIneChange(this.value);
}

class ExternalNumberChange extends FollowerFormEvent {
  final String value;
  const ExternalNumberChange(this.value);
}

class InternalNumberChange extends FollowerFormEvent {
  final String value;
  const InternalNumberChange(this.value);
}

class ZipCodeChange extends FollowerFormEvent {
  final String value;
  const ZipCodeChange(this.value);
}

class NeighborhoodChange extends FollowerFormEvent {
  final String value;
  const NeighborhoodChange(this.value);
}

class LoadIneData extends FollowerFormEvent {
  final String value;
  const LoadIneData(this.value);
}

class NamesChange extends FollowerFormEvent {
  final String value;
  const NamesChange(this.value);
}

class LastNameChange extends FollowerFormEvent {
  final String value;
  const LastNameChange(this.value);
}

class SurNameChange extends FollowerFormEvent {
  final String value;
  const SurNameChange(this.value);
}

class PhoneChange extends FollowerFormEvent {
  final String value;
  const PhoneChange(this.value);
}

class PhoneOtherChange extends FollowerFormEvent {
  final String value;
  const PhoneOtherChange(this.value);
}

/// El puesto donde queda el convencido. Si es de una sección, la sección se
/// fija a ésa.
class TargetChange extends FollowerFormEvent {
  final int positionId;
  final String label;
  final int? sectionGeoId;
  const TargetChange(
      {required this.positionId, required this.label, this.sectionGeoId});
}

class CurpChange extends FollowerFormEvent {
  final String value;
  const CurpChange(this.value);
}

class IneChange extends FollowerFormEvent {
  final String value;
  const IneChange(this.value);
}

class EmailChange extends FollowerFormEvent {
  final String value;
  const EmailChange(this.value);
}

class ExpirationYearChange extends FollowerFormEvent {
  final String value;
  const ExpirationYearChange(this.value);
}

class BirthdayChange extends FollowerFormEvent {
  final String value;
  const BirthdayChange(this.value);
}

class ResetForm extends FollowerFormEvent {
  const ResetForm();
}
