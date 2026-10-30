import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:electorama_app/domain/entities/geo/locality.dart';
import 'package:electorama_app/domain/entities/geo/municipality.dart';
import 'package:electorama_app/domain/entities/geo/section.dart';
import 'package:electorama_app/domain/repositories/geo/geo_repository.dart';

part 'geography_event.dart';
part 'geography_state.dart';

class GeographyBloc extends Bloc<GeographyEvent, GeographyState> {
  //final token = KeyValueStorageServiceImpl.getValue<String>('token');
  GeoRepository geoRepository;

  GeographyBloc(this.geoRepository) : super(SectionsInitial()) {
    on<LoadSections>(_loadSections);
    on<LoadMunicipality>(_loadMunicipality);
    on<LoadLocalities>(_loadLocalities);
  }
  void _loadSections(LoadSections event, Emitter<GeographyState> emit) async{
    try {
        emit(state.copyWith(loading: true));
        final resp = await geoRepository.loadSections(event.seccion);
      
        emit(state.copyWith(listSections: resp, loading: false));
      } catch (error) {
        try{
          if(error.toString().contains('Unique index violated')){
            emit(state.copyWith(loading: false, error: 'Ya registrado'));
          }
        }catch(e){
          emit(state.copyWith(loading: false, error:'Ocurrio un error'));
        }
      }
  }
  void _loadMunicipality(LoadMunicipality event, Emitter<GeographyState> emit) async{
    try {
        emit(state.copyWith(municipality: null));
        emit(state.copyWith(loading: true));
        final resp = await geoRepository.loadMunicipality(event.seccionId);
        emit(state.copyWith(municipality: resp, loading: false));
      } catch (error) {
        try{
          if(error.toString().contains('Unique index violated')){
            emit(state.copyWith(loading: false, error: 'Ya registrado'));
          }
        }catch(e){
          emit(state.copyWith(loading: false, error:'Ocurrio un error'));
        }
      }
  }
  void _loadLocalities(LoadLocalities event, Emitter<GeographyState> emit) async{
    try {
        emit(state.copyWith(listLocalities: []));
        emit(state.copyWith(loading: true));
        final resp = await geoRepository.loadLocalities(event.seccionId);
        emit(state.copyWith(listLocalities: resp, loading: false));
      } catch (error) {
        try{
          if(error.toString().contains('Unique index violated')){
            emit(state.copyWith(loading: false, error: 'Ya registrado'));
          }
        }catch(e){
          emit(state.copyWith(loading: false, error:'Ocurrio un error'));
        }
      }
  }
}
