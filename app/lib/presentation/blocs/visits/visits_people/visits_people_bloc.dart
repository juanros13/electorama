import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/domain/entities/visits/visit_people.dart';
import 'package:electorama_app/domain/repositories/storage/local_storage_repository.dart';
import 'package:electorama_app/infrastructure/datasources/visits/local_storage_visit_datasource_impl.dart';
import 'package:electorama_app/infrastructure/repositories/storage/local_storage_repository_impl.dart';

part 'visits_people_event.dart';
part 'visits_people_state.dart';

class VisitsPeopleBloc extends Bloc<VisitsPeopleEvent, VisitsPeopleState> {
  LocalStorageRepository localStorageRepository = LocalStorageRepositoryImpl(LocalStorageVisitDataSourceImpl());
  
  VisitsPeopleBloc() : super(const VisitsPeopleState()) {
    on<InitVisitedPeople>((event, emit)  => emit(state.copyWith(loading: false, add: false, error: '')));
    on<LoadVisitPeople>(((event, emit) async {
      emit(state.copyWith(loading: true));
      final resp = await localStorageRepository.loadVisits();
      
      emit(state.copyWith(listVisitPeople: resp, loading: false));
    }));
    on<AddVisitPeople>(_addVisitPeople);
    on<DeleteVisitPeople>(_deleteVisitPeople);
    on<SyncVisitPeople>(_syncVisitPeople);
  }
  
  void _addVisitPeople(AddVisitPeople event, Emitter<VisitsPeopleState> emit) async{
    try {
        emit(state.copyWith(loading: true));
        await localStorageRepository.insertVisitPeople(event.visitPeople);
        emit(state.copyWith(loading: false, add: true));
        add(const LoadVisitPeople());
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
  void _deleteVisitPeople(DeleteVisitPeople event, Emitter<VisitsPeopleState> emit) async{
    try {
        emit(state.copyWith(loading: true));
        await localStorageRepository.deleteVisitPeople(event.isarId);
        emit(state.copyWith(loading: false));
        add(const LoadVisitPeople());
      } catch (error) {
        try{
          emit(state.copyWith(loading: false, error: 'Ya registrado'));
        }catch(e){
          emit(state.copyWith(loading: false, error:'Ocurrio un error'));
        }
      }
  }
  void _syncVisitPeople(SyncVisitPeople event, Emitter<VisitsPeopleState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      await localStorageRepository.insertVisitPeople(event.visit);
      emit(state.copyWith(loading: false, add: true));
    } catch (e) {
      emit(state.copyWith(loading: false, error:'Ocurrio un error'));
    }
  }
}


