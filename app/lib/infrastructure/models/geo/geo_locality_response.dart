// To parse this JSON data, do
//
//     final geoLocalityResponse = geoLocalityResponseFromJson(jsonString);

import 'dart:convert';

GeoLocalityResponse geoLocalityResponseFromJson(String str) => GeoLocalityResponse.fromJson(json.decode(str));

String geoLocalityResponseToJson(GeoLocalityResponse data) => json.encode(data.toJson());

class GeoLocalityResponse {
    final List<LocalitiesResponse> localitiesResponse;

    GeoLocalityResponse({
        required this.localitiesResponse,
    });

    factory GeoLocalityResponse.fromJson(Map<String, dynamic> json) => GeoLocalityResponse(
        localitiesResponse: List<LocalitiesResponse>.from(json["LocalitiesSections"].map((x) => LocalitiesResponse.fromJson(x))),
    );

    Map<String, dynamic> toJson() => {
        "LocalitiesSections": List<dynamic>.from(localitiesResponse.map((x) => x.toJson())),
    };
}

class LocalitiesResponse {
    final int id;
    final String municipio;
    final String seccion;
    final String localidad;
    final int seccionId;
    final dynamic deleted;

    LocalitiesResponse({
        required this.id,
        required this.municipio,
        required this.seccion,
        required this.localidad,
        required this.seccionId,
        this.deleted,
    });

    factory LocalitiesResponse.fromJson(Map<String, dynamic> json) => LocalitiesResponse(
        id: json["id"],
        municipio: json["municipio"],
        seccion: json["seccion"],
        localidad: json["localidad"],
        seccionId: json["seccionId"],
        deleted: json["deleted"],
    );

    Map<String, dynamic> toJson() => {
        "id": id,
        "municipio": municipio,
        "seccion": seccion,
        "localidad": localidad,
        "seccionId": seccionId,
        "deleted": deleted,
    };
}
