import 'dart:io';
import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path_provider/path_provider.dart';

import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';
import 'package:electorama_app/domain/repositories/canvas_wall/service_canvas_wall_repository.dart';
import 'package:electorama_app/domain/repositories/canvas_wall/local_storage_canvas_wall_repository.dart';
import 'package:electorama_app/infrastructure/datasources/canvas_wall/local_storage_canvas_wall_datasource_impl.dart';
import 'package:electorama_app/infrastructure/repositories/canvas_wall/local_storage_canvas_wall_repository_impl.dart';
import 'package:electorama_app/infrastructure/models/canvas_wall/canvas_wall_model.dart';

part 'canvas_wall_service_event.dart';
part 'canvas_wall_service_state.dart';

class CanvasWallServiceBloc extends Bloc<CanvasWallServiceEvent, CanvasWallServiceState> {

  ServiceCanvasWallRepository serviceCanvasWallRepository;

  LocalStorageCanvasWallRepository localStorageCanvasWallRepository =
  LocalStorageCanvasWallRepositoryImpl(LocalStorageCanvasWallDatasourceImpl());

  CanvasWallServiceBloc(this.serviceCanvasWallRepository) : super(const CanvasWallServiceState()) {
    on<SentCanvasWallService>(_sentCanvasWallService);
    on<ChangeListCanvasWall>(_changeListCanvasWall);
    on<ChangeSubmitCanvasWallSync> (_changeSubmitSync);
  }

  Future<void> _sentCanvasWallService(SentCanvasWallService event, Emitter<CanvasWallServiceState> emit) async {
    try {
      emit(state.copyWith(loading: true));

      int canvasWallSync = 0;
      int canvasWallError = 0;
      final int sectionSelected = int.parse(event.section);
      final int municipalitySelected = int.parse(event.municipality);
      final int localitySelected = int.parse(event.locality);


      for(final canvasWall in state.listCanvasWall) {
        final directory = await getApplicationDocumentsDirectory();
        final File file = await File('${directory.path}/my_image.jpg').writeAsBytes(canvasWall.file!);

        final newCanvasWall = CanvasWallModel(
          municipalityId: municipalitySelected,
          seccionId: sectionSelected,
          localityId: localitySelected,
          coordinateZoneId: event.coordinateZoneId,
          referenceAuthId: event.referenceAuthId,

          uuid: canvasWall.uuid,
          deviceId: canvasWall.deviceId,
          dateOfPlacement: canvasWall.date,
          canvasAndWallsCategoryId: int.parse(canvasWall.categoryId),
          canvasAndWallsProyectId: int.parse(canvasWall.projectId),
          file: file,
          latitude: canvasWall.latitude,
          longitude: canvasWall.longitude,
        );
        final canvasWallJson = await newCanvasWall.toFormData();

        final isSync = await serviceCanvasWallRepository.postCanvasWall(canvasWallJson, canvasWall.isarId!);

        if(isSync) {
          canvasWallSync++;
          localStorageCanvasWallRepository.syncCanvasWall(canvasWall.isarId!);
        } else {
          canvasWallError++;
        }

      }


      emit(state.copyWith(
        loading: false,
        listCanvasWall: await localStorageCanvasWallRepository.loadCanvasWall(),
        submitSync:true,
        canvasWallSync: canvasWallSync,
        canvasWallError: canvasWallError,
      ));
    } catch (e) {
      print(e);
    }
  }

  void _changeListCanvasWall(ChangeListCanvasWall event, Emitter<CanvasWallServiceState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      // print('Copie la lista de visited people');
      emit(state.copyWith(
        loading: false,
        listCanvasWall: event.listCanvasWall
      ));

    } catch (error) {
      emit(state.copyWith(loading: false, error:'Ocurrio un error'));
    }
  }

  void _changeSubmitSync(ChangeSubmitCanvasWallSync event, Emitter<CanvasWallServiceState> emit) async {
    emit(state.copyWith(
      submitSync: event.value,
    ));

  }


}