import 'package:electorama_app/domain/entities/visits/visit_people.dart';

abstract class VisitPostRepository {

  Future<List<VisitPeople>> getAllVisits();

}