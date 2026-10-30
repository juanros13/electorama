import 'package:electorama_app/domain/entities/geo/municipality.dart';
import 'package:electorama_app/infrastructure/models/geo/geo_municipality_response.dart';

class MunicipalityMapper {
  static Municipality municipalityResponseToEntity(
          GeoMunicipalityResponse geoSectionDB) =>
      Municipality(
          id: geoSectionDB.id,
          seccion: geoSectionDB.seccion,
          distritoLocalId: geoSectionDB.distritoLocalId,
          distritoFederalId: geoSectionDB.distritoFederalId,
          municipioId: geoSectionDB.municipioId,
          entidadId: geoSectionDB.entidadId,
          nomMun: geoSectionDB.nomMun,
          nomEnt: geoSectionDB.nomEnt,
          meta: geoSectionDB.meta);
}
