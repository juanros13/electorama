part of 'visits_bloc.dart';

abstract class VisitsEvent {
  const VisitsEvent();
}

class SetVisit extends VisitsEvent {
  final int isarId;
  final int sectionSelected ;
  final int municipalitySelected;
  final int localitySelected;
  final String coordinateZoneId;
  final String referenceAuthId;

  const SetVisit({
    required this.sectionSelected, 
    required this.municipalitySelected, 
    required this.localitySelected, 
    required this.isarId,
    required this.coordinateZoneId,
    required this.referenceAuthId
  });
}

class PostVisit extends VisitsEvent {
  final VisitedModel visited;
  final int isarId;

  const PostVisit({
    required this.visited,
    required this.isarId
  });
}
class ChangeListVisitedPeople extends VisitsEvent{
  final List<VisitPeople> listVisitsPeople;

  ChangeListVisitedPeople({
    required this.listVisitsPeople
  });

}
class SendVisit extends VisitsEvent {
  final String section;
  final String municipality; 
  final String locality;
  final String coordinateZoneId;
  final String referenceAuthId;

  const SendVisit({
    required this.section,
    required this.municipality,
    required this.locality,
    required this.coordinateZoneId,
    required this.referenceAuthId
  });
}

class ChangeSubmitSync extends VisitsEvent{
  final bool value;

  ChangeSubmitSync({
    required this.value
  });

}