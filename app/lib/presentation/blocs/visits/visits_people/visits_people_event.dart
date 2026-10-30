part of 'visits_people_bloc.dart';

abstract class VisitsPeopleEvent  {
  const VisitsPeopleEvent();
}

class InitVisitedPeople extends VisitsPeopleEvent{}

class LoadVisitPeople extends VisitsPeopleEvent{
  const LoadVisitPeople();
}


class AddVisitPeople extends VisitsPeopleEvent{
  final VisitPeople visitPeople;
  const AddVisitPeople({
    required this.visitPeople
  });
}

class DeleteVisitPeople extends VisitsPeopleEvent{
  final int isarId;
  const DeleteVisitPeople({
    required this.isarId
  });
}

class SyncVisitPeople extends VisitsPeopleEvent {
  final VisitPeople visit;
  const SyncVisitPeople({
    required this.visit
  });
}