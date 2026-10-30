// To parse this JSON data, do
//
//     final geoSectionResponse = geoSectionResponseFromJson(jsonString);

import 'dart:convert';

List<GeoSectionDB> geoSectionResponseFromJson(List<dynamic> str) => List<GeoSectionDB>.from(str.map((x) => GeoSectionDB.fromJson(x)));

String geoSectionResponseToJson(List<GeoSectionDB> data) => json.encode(List<dynamic>.from(data.map((x) => x.toJson())));

class GeoSectionDB {
    final int id;
    final String userAuthId;
    final String seccionNumber;
    final int seccionGeoId;
    final int meta;
    final DateTime? deleted;
    final int municipalityId;

    GeoSectionDB({
        required this.id,
        required this.userAuthId,
        required this.seccionNumber,
        required this.seccionGeoId,
        required this.meta,
        this.deleted,
        required this.municipalityId,
    });

    factory GeoSectionDB.fromJson(Map<String, dynamic> json) => GeoSectionDB(
        id: json["id"],
        userAuthId: json["userAuthId"],
        seccionNumber: json["seccionNumber"],
        seccionGeoId: json["seccionGeoId"],
        meta: json["meta"],
        deleted: json["deleted"] == null ? null : DateTime.parse(json["deleted"]),
        municipalityId: json["municipalityId"],
    );

    Map<String, dynamic> toJson() => {
        "id": id,
        "userAuthId": userAuthId,
        "seccionNumber": seccionNumber,
        "seccionGeoId": seccionGeoId,
        "meta": meta,
        "deleted": "${deleted!.year.toString().padLeft(4, '0')}-${deleted!.month.toString().padLeft(2, '0')}-${deleted!.day.toString().padLeft(2, '0')}",
        "municipalityId": municipalityId,
    };
}
