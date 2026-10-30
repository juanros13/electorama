import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:intl/intl.dart';
import 'package:timezone/standalone.dart' as tz;
import 'package:electorama_app/domain/entities/visits/visit_people.dart';
import 'package:electorama_app/domain/repositories/storage/local_storage_repository.dart';
import 'package:electorama_app/domain/repositories/visits/visited_repository.dart';
import 'package:electorama_app/infrastructure/datasources/visits/local_storage_visit_datasource_impl.dart';
import 'package:electorama_app/infrastructure/models/visits/visited_model.dart';
import 'package:electorama_app/infrastructure/repositories/storage/local_storage_repository_impl.dart';
import 'package:electorama_app/presentation/blocs/coordinator_zone/coordinator_zone_form_bloc.dart';

part 'visits_event.dart';
part 'visits_state.dart';


class VisitsBloc extends Bloc<VisitsEvent, VisitsState> {
  
  VisitedRepository visitedRepository;
  LocalStorageRepository localStorageRepository = LocalStorageRepositoryImpl(LocalStorageVisitDataSourceImpl());
  
  final visitCordinator = BlocProvider(create: (context) => CordinatorZoneFormBloc());
  VisitsBloc(this.visitedRepository) : super(const VisitsState()) {
    on<SetVisit>(_setVisit);
    on<PostVisit>(_postVisit);
    on<SendVisit>(_sendVisit);
    on<ChangeListVisitedPeople>(_changeListVisitedPeople);
    on<ChangeSubmitSync> (_changeSubmitSync);
  }
  void _changeSubmitSync(ChangeSubmitSync event, Emitter<VisitsState> emit) async {
    emit(state.copyWith(
      submitSync: event.value,
    ));

  }
  void _changeListVisitedPeople(ChangeListVisitedPeople event, Emitter<VisitsState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      print('Copie la lista de visited people');
      emit(state.copyWith(
        loading: false,
        listVisitPeople: event.listVisitsPeople
      ));

    } catch (error) {
      emit(state.copyWith(loading: false, error:'Ocurrio un error'));
    }
  }
  void _setVisit(SetVisit event, Emitter<VisitsState> emit) async {
    try {
        emit(state.copyWith(loading: true));
        final isarId = event.isarId;
        final int sectionSelected = event.sectionSelected;
        // final int municipalitySelected = event.municipalitySelected;
        final int localitySelected = event.localitySelected;
        final visitPeople = await localStorageRepository.getVisitPeopleById(isarId);

        final formato = DateFormat('yyyy-MM-dd');
        String fechaTexto = formato.format(visitPeople.created!);

        final newVisited = VisitedModel(
          municipalityId: 1977,
          seccionId: sectionSelected,
          coordinateZoneId: '1393b13f-c498-4be1-9bae-d27ecb75e8a5',
          localityId: localitySelected,
          visitDate: fechaTexto,
          numberOfVisited: int.parse(visitPeople.visitNumber),
          userCreatedId: visitPeople.deviceId,
          visitedPeople: [visitPeople],
          referenceAuthId: "956087c2-6f56-45ca-a15e-1348df025df4"
        );
        emit(state.copyWith(loading: false, visited: newVisited));

      } catch (error) {
        emit(state.copyWith(loading: false, error:'Ocurrio un error'));
      }
  }


  Future<void> _postVisit(PostVisit event, Emitter<VisitsState> emit) async {
    try {
      emit(state.copyWith(loading: true));

      // final visitJson = event.visited.toJson();
      // localStorageRepository.postVisited(visitJson);
      // await visitedRepository.postVisited(visitJson,);
      // localStorageRepository.syncVisit(event.isarId);

      emit(state.copyWith(loading: false));

    } catch (e) {
      print(e);
    }
  }

  Future<void> _sendVisit(SendVisit event, Emitter<VisitsState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      print('Entre a envio de visitas');
      //final isarId = event.isarId;
      final int sectionSelected = int.parse(event.section);
      final int municipalitySelected = int.parse(event.municipality);
      final int localitySelected = int.parse(event.locality);
      var mex = tz.getLocation('America/Mexico_City');


      for(final visit in state.listVisitPeople){
        
        final dateCreated = DateFormat('yyyy-MM-dd').format( tz.TZDateTime.from(visit.created!, mex));
        final newVisited = VisitedModel(
          municipalityId: municipalitySelected,
          seccionId: sectionSelected,
          coordinateZoneId: event.coordinateZoneId,
          localityId: localitySelected,
          visitDate: dateCreated,
          numberOfVisited: int.parse(visit.visitNumber),
          userCreatedId: visit.deviceId,
          visitedPeople: [visit],
          referenceAuthId: event.referenceAuthId
        );
        final visitJson = newVisited.toJson();
        await visitedRepository.postVisited(visitJson, visit.isarId!);
        // await localStorageRepository.syncVisit(visit.isarId!);
      }

      emit(state.copyWith(
        loading: false,
        listVisitPeople: [],
        submitSync:true
      ));
    } catch (e) {
      print(e);
    }
  }
}