import 'package:electorama_app/domain/entities/geo/locality.dart';
import 'package:electorama_app/domain/entities/geo/municipality.dart';
import 'package:electorama_app/domain/entities/geo/section.dart';

abstract class GeoRepository {

    Future<List<Section>> loadSections(String authId);
    Future<Municipality> loadMunicipality(int seccionId);
    Future<List<Locality>> loadLocalities(int seccionId);


}