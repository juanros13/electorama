// To parse this JSON data, do
//
//     final geoMunicipalityResponse = geoMunicipalityResponseFromJson(jsonString);


class GeoMunicipalityResponse {
    final int id;
    final int seccion;
    final int distritoLocalId;
    final int distritoFederalId;
    final int municipioId;
    final int entidadId;
    final String nomMun;
    final String nomEnt;
    final int meta;

    GeoMunicipalityResponse({
        required this.id,
        required this.seccion,
        required this.distritoLocalId,
        required this.distritoFederalId,
        required this.municipioId,
        required this.entidadId,
        required this.nomMun,
        required this.nomEnt,
        required this.meta,
    });

    factory GeoMunicipalityResponse.fromJson(Map<String, dynamic> json) => GeoMunicipalityResponse(
        id: json["id"],
        seccion: json["seccion"],
        distritoLocalId: json["distritoLocalId"],
        distritoFederalId: json["distritoFederalId"],
        municipioId: json["municipioId"],
        entidadId: json["entidadId"],
        nomMun: json["nomMun"],
        nomEnt: json["nomEnt"],
        meta: json["meta"],
    );

    Map<String, dynamic> toJson() => {
        "id": id,
        "seccion": seccion,
        "distritoLocalId": distritoLocalId,
        "distritoFederalId": distritoFederalId,
        "municipioId": municipioId,
        "entidadId": entidadId,
        "nomMun": nomMun,
        "nomEnt": nomEnt,
        "meta": meta,
    };
}
