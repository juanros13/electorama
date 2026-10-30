import 'package:electorama_app/domain/entities/geo/locality.dart';
import 'package:electorama_app/infrastructure/models/geo/geo_locality_response.dart';

class LocalityMapper {
  static Locality localityResponseToEntity(
          LocalitiesResponse localitiesResponse) =>
      Locality(
          id: localitiesResponse.id,
          municipio: localitiesResponse.municipio,
          seccion: localitiesResponse.seccion,
          localidad: localitiesResponse.localidad,
          seccionId: localitiesResponse.seccionId);
}
