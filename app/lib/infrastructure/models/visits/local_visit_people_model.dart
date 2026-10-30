// To parse this JSON data, do
//
//     final localVisitPeople = localVisitPeopleFromJson(jsonString);

import 'dart:convert';

LocalVisitPeople localVisitPeopleFromJson(String str) => LocalVisitPeople.fromJson(json.decode(str));

String localVisitPeopleToJson(LocalVisitPeople data) => json.encode(data.toJson());

class LocalVisitPeople {
    final String uuid; 
    final bool openTheDoor;
    final String street;
    final int internalNumber;
    final String longitude;
    final String latitude;
    final String names;
    final String lastName;
    final String surName;
    final int adultsInHome;
    final String phone;
    final bool deliveredGazette;
    final int visitNumber;
    final String notes;
    final DateTime created;


    LocalVisitPeople({
        required this.uuid,
        required this.openTheDoor,
        required this.street,
        required this.internalNumber,
        required this.longitude,
        required this.latitude,
        required this.names,
        required this.lastName,
        required this.surName,
        required this.adultsInHome,
        required this.phone,
        required this.deliveredGazette,
        required this.visitNumber,
        required this.notes,
        required this.created,
    });

    factory LocalVisitPeople.fromJson(Map<String, dynamic> json) => LocalVisitPeople(
        uuid: json["uuid"],
        openTheDoor: json["openTheDoor"],
        street: json["street"],
        internalNumber: json["internalNumber"],
        longitude: json["longitude"],
        latitude: json["latitude"],
        names: json["names"],
        lastName: json["lastName"],
        surName: json["surName"],
        adultsInHome: json["adultsInHome"],
        phone: json["phone"],
        deliveredGazette: json["deliveredGazette"],
        visitNumber: json["visitNumber"],
        notes: json["notes"],
        created: json["created"]
    );

    Map<String, dynamic> toJson() => {
        "uuid": uuid,
        "openTheDoor": openTheDoor,
        "street": street,
        "internalNumber": internalNumber,
        "longitude": longitude,
        "latitude": latitude,
        "names": names,
        "lastName": lastName,
        "surName": surName,
        "adultsInHome": adultsInHome,
        "phone": phone,
        "deliveredGazette": deliveredGazette,
        "visitNumber": visitNumber,
        "notes": notes,
        "created": created
    };
}
