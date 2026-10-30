import 'package:electorama_app/domain/datasources/geo/geo_datasource.dart';
import 'package:electorama_app/domain/entities/geo/locality.dart';
import 'package:electorama_app/domain/entities/geo/municipality.dart';
import 'package:electorama_app/domain/entities/geo/section.dart';
import 'package:electorama_app/domain/repositories/geo/geo_repository.dart';

class GeoRepositoryImpl extends GeoRepository{

  final GeoDatasaource datasaource;

  GeoRepositoryImpl(this.datasaource);


  @override
  Future<List<Section>> loadSections(String authId) {
    return datasaource.loadSections(authId);
  }

  @override
  Future<Municipality> loadMunicipality(int seccionId) {
    return datasaource.loadMunicipality(seccionId);
  }

  @override
  Future<List<Locality>> loadLocalities(int seccionId) {
    return datasaource.loadLocalities(seccionId);
  }

}