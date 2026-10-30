
import 'package:electorama_app/domain/entities/visits/visit_post.dart';

class LocalVisitModel {

  final int municipalityId;
  final int sectionId;
  final int coordinateZoneId;
  final int localityId;
  final String visitDate;
  final int numberOfVisited;
  final String userCreatedId;
  final List<String> visitedPeople;

  LocalVisitModel({
    required this.municipalityId, 
    required this.sectionId, 
    required this.coordinateZoneId, 
    required this.localityId, 
    required this.visitDate, 
    required this.numberOfVisited, 
    required this.userCreatedId, 
    required this.visitedPeople
  });

  factory LocalVisitModel.fromJson(Map<String, dynamic> json) => LocalVisitModel(
            municipalityId  : json["municipalityId"], 
            sectionId       : json["sectionId"], 
            coordinateZoneId: json["coordinateZoneId"], 
            localityId      : json["localityId"], 
            visitDate       : json["visitDate"], 
            numberOfVisited : json["numberOfVisited"], 
            userCreatedId   : json["userCreatedId"], 
            visitedPeople   : json["visitedPeople"]);

  VisitPost toVisitPostEntity() => VisitPost(
      municipalityId  : municipalityId, 
      sectionId       : sectionId, 
      coordinateZoneId: coordinateZoneId, 
      localityId      : localityId, 
      visitDate       : visitDate, 
      numberOfVisited : numberOfVisited, 
      userCreatedId   : userCreatedId, 
      visitedPeople   : visitedPeople
    );

}