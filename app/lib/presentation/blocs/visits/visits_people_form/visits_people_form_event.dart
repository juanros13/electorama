part of 'visits_people_form_bloc.dart';

abstract class VisitPeopleEvent extends Equatable {
  const VisitPeopleEvent();

  @override
  List<Object> get props => [];
}


class StreetChange extends VisitPeopleEvent{
  final String value;
  const StreetChange(this.value);
}

class  ExternalNumberChange extends VisitPeopleEvent{
  final String value;
  const ExternalNumberChange(this.value);
}

class SubmitVisitedPeople extends VisitPeopleEvent{
  const SubmitVisitedPeople();
}


class CompleteSubmitChange extends VisitPeopleEvent{
  final bool value;
  const CompleteSubmitChange(this.value);
}

class OpenTheDoorChange extends VisitPeopleEvent{
  final bool value;
  const OpenTheDoorChange(this.value);
}

class LatitudeChange extends VisitPeopleEvent{
  final String value;
  const LatitudeChange(this.value);
}

class LongitudeChange extends VisitPeopleEvent{
  final String value;
  const LongitudeChange(this.value);
}

class SexChange extends VisitPeopleEvent{
  final Sex value;
  const SexChange(this.value);
}

class AgeChange extends VisitPeopleEvent{
  final String value;
  const AgeChange(this.value);
}


class HasSympathyChange extends VisitPeopleEvent{
  final String value;
  const HasSympathyChange(this.value);
}


class NamesChange extends VisitPeopleEvent{
  final String value;
  const NamesChange(this.value);
}

class LastNameChange extends VisitPeopleEvent{
  final String value;
  const LastNameChange(this.value);
}

class SurNameChange extends VisitPeopleEvent{
  final String value;
  const SurNameChange(this.value);
}

class AccepInformationChange extends VisitPeopleEvent{
  final bool value;
  const AccepInformationChange(this.value);
}

class PhoneChange extends VisitPeopleEvent{
  final String value;
  const PhoneChange(this.value);
}




