import 'package:electorama_app/domain/entities/visits/visit_people.dart';

class VisitedEntity {

  final int municipalityId;
  final int seccionId;
  final String coordinateZoneId;
  final int localityId;
  final String visitDate;
  final int numberOfVisited;
  final String userCreatedId;
  final List<VisitPeople> visitedPeople;
  final String referenceAuthId;

  VisitedEntity({
    required this.municipalityId, 
    required this.seccionId, 
    required this.coordinateZoneId, 
    required this.localityId, 
    required this.visitDate, 
    required this.numberOfVisited, 
    required this.userCreatedId, 
    required this.visitedPeople, 
    required this.referenceAuthId
  });
}