import 'package:electorama_app/domain/entities/visits/visit_people.dart';

class VisitedModel {

  final int municipalityId;
  final int seccionId;
  final String coordinateZoneId;
  final int localityId;
  final String visitDate;
  final int numberOfVisited;
  final String userCreatedId;
  final List<VisitPeople> visitedPeople;
  final String referenceAuthId;

  VisitedModel({
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

  factory VisitedModel.fromJson(Map<String, dynamic> json) => VisitedModel(
    municipalityId: json["municipalityId"],
    seccionId: json["seccionId"],
    coordinateZoneId: json["coordinateZoneId"],
    localityId: json["localityId"],
    visitDate: json["visitDate"],
    numberOfVisited: json["numberOfVisited"],
    userCreatedId: json["userCreatedId"],
    visitedPeople: [],
    referenceAuthId: json["referenceAuthId"],
  );

  Map<String, dynamic> toJson() => {
    "municipalityId": municipalityId,
    "seccionId": seccionId,
    "coordinateZoneId": coordinateZoneId,
    "localityId": localityId,
    "visitDate": visitDate,
    "numberOfVisited": numberOfVisited,
    "userCreatedId": userCreatedId,
    "visitedPeople": [
        !visitedPeople[0].openTheDoor ?
          {
            "uuid" : visitedPeople[0].uuid,
            "openTheDoor": visitedPeople[0].openTheDoor,
            "street": visitedPeople[0].street,
            "externalNumber": visitedPeople[0].externallNumber,
            "longitude": visitedPeople[0].longitude,
            "latitude": visitedPeople[0].latitude,
            "visitNumber": 10
          }
        :
          {
            "uuid" : visitedPeople[0].uuid,
            "openTheDoor": visitedPeople[0].openTheDoor,
            "street": visitedPeople[0].street,
            "externalNumber": visitedPeople[0].externallNumber,
            "longitude": visitedPeople[0].longitude,
            "latitude": visitedPeople[0].latitude,
            "names": visitedPeople[0].names??"",
            "lastName": visitedPeople[0].lastName??"",
            "surName": visitedPeople[0].surName??"",
            "phone": visitedPeople[0].phone??"",
            "isRelated": visitedPeople[0].hasSympathy == 'Si' ? true : false,
            "ageOfThePersonWhoOpensTheDoor": visitedPeople[0].age,
            "sexOfTheOneWhoOpensTheDoor": visitedPeople[0].sex ,
            "visitNumber": 10,
            // "deliveredGazette": true,
            // "adultsInHome": 1,
            // "notes": "xxx", 
          }
    ],
    "referenceAuthId": referenceAuthId,
  };
}