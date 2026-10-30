import 'package:electorama_app/domain/entities/visits/visit_people.dart';

abstract class LocalStorageRepository {

  Future<List<VisitPeople>> loadVisits();
  Future<VisitPeople> insertVisitPeople(VisitPeople visitPeople);
  Future<bool> deleteVisitPeople(int isarId);
  Future<VisitPeople> getVisitPeopleById(int isarId);
  Future<void> postVisited(Map<String, dynamic> visitedJson);
  Future<VisitPeople> syncVisit(int isarId);

}