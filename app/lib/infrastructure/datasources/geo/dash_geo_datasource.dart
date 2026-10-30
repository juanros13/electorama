import 'package:dio/dio.dart';
import 'package:electorama_app/config/constants/environment.dart';
import 'package:electorama_app/domain/datasources/geo/geo_datasource.dart';
import 'package:electorama_app/domain/entities/geo/locality.dart';
import 'package:electorama_app/domain/entities/geo/municipality.dart';
import 'package:electorama_app/domain/entities/geo/section.dart';
import 'package:electorama_app/infrastructure/mappers/geo/locality_mapper.dart';
import 'package:electorama_app/infrastructure/mappers/geo/municipality_mapper.dart';
import 'package:electorama_app/infrastructure/mappers/geo/section_mapper.dart';
import 'package:electorama_app/infrastructure/models/geo/geo_locality_response.dart';
import 'package:electorama_app/infrastructure/models/geo/geo_municipality_response.dart';
import 'package:electorama_app/infrastructure/models/geo/geo_section_db.dart';

class DashGeoDatasouce extends GeoDatasaource{
  late final Dio dioUser;
  late final Dio dioGeography;
  final String accessToken;


  DashGeoDatasouce({
    required this.accessToken
  }): dioUser = Dio(
    BaseOptions(
      baseUrl: Environment.urlUser,
      headers: {
        'Authorization': 'Bearer $accessToken'
      }
    )
  ),dioGeography = Dio(
    BaseOptions(
      baseUrl: Environment.urlGeography,
      headers: {
        'Authorization': 'Bearer $accessToken'
      }
    )
  );

  @override
  Future<List<Section>> loadSections(String authId) async{
    try {
      final response = await dioUser.get('/v1.0.0/userSeccion/$authId');
      if (response.statusCode == 200) {
        final seccionList = geoSectionResponseFromJson(response.data);
        final List<Section> sections = seccionList.map(
          (section) {
            return SectionMapper.sectionDBToEntity(section);
          }
        ).toList();
        return sections;
      }else{
        return [];
      }
    } catch (e) {
      return [];
    }
  }

  @override
  Future<Municipality> loadMunicipality(int seccionId)async {
    try {
      final response = await dioGeography.get('/seccion/$seccionId');
      if (response.statusCode == 200) {
        //final municipality = geoSectionResponseFromJson(response.data);
        final municipalityResponse = GeoMunicipalityResponse.fromJson(response.data);
        final Municipality municipality = MunicipalityMapper.municipalityResponseToEntity(municipalityResponse);
        return municipality;
      }else{
        return Municipality(id: 0, seccion: 0, distritoLocalId: 0, distritoFederalId:  0, municipioId:  0, entidadId:  0, nomMun:  '', nomEnt:  '', meta: 123);
      }
    } catch (e) {
      return Municipality(id: 0, seccion: 0, distritoLocalId: 0, distritoFederalId:  0, municipioId:  0, entidadId:  0, nomMun:  '', nomEnt:  '', meta: 123);
    }
  }

  @override
  Future<List<Locality>> loadLocalities(int seccionId) async {
    try {
      final response = await dioGeography.get('/localitiesSection/section/$seccionId');
      if (response.statusCode == 200) {
        final geolocalitiesResponse = GeoLocalityResponse.fromJson(response.data);
        final List<Locality> localities = geolocalitiesResponse.localitiesResponse.map(
          (section) {
            return LocalityMapper.localityResponseToEntity(section);
          }
        ).toList();
        return localities;
      }else{
        return [];
      }
    } catch (e) {
      return [];
    }
  }

}