import 'package:electorama_app/domain/entities/geo/section.dart';
import 'package:electorama_app/infrastructure/models/geo/geo_section_db.dart';

class SectionMapper {
  static Section sectionDBToEntity(GeoSectionDB geoSectionDB) => Section(
      id: geoSectionDB.id,
      userAuthId: geoSectionDB.userAuthId,
      seccionNumber: geoSectionDB.seccionNumber,
      seccionGeoId: geoSectionDB.seccionGeoId,
      meta: geoSectionDB.meta,
      municipalityId: geoSectionDB.municipalityId);
}
