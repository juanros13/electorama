class VisitPost {

  int? isarId;

  final int municipalityId;
  final int sectionId;
  final int coordinateZoneId;
  final int localityId;
  final String visitDate;
  final int numberOfVisited;
  final String userCreatedId;
  final List<String> visitedPeople;

  VisitPost({
    required this.municipalityId, 
    required this.sectionId, 
    required this.coordinateZoneId, 
    required this.localityId, 
    required this.visitDate, 
    required this.numberOfVisited, 
    required this.userCreatedId, 
    required this.visitedPeople
  });

}