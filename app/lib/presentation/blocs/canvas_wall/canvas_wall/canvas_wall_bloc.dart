import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/domain/repositories/canvas_wall/local_storage_canvas_wall_repository.dart';
import 'package:electorama_app/infrastructure/datasources/canvas_wall/local_storage_canvas_wall_datasource_impl.dart';
import 'package:electorama_app/infrastructure/repositories/canvas_wall/local_storage_canvas_wall_repository_impl.dart';
import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';

part 'canvas_wall_event.dart';
part 'canvas_wall_state.dart';

class CanvasWallBloc extends Bloc<CanvasWallEvent, CanvasWallState> {
  
  // ServiceCanvasWallRepository serviceCanvasWallRepository;

  LocalStorageCanvasWallRepository localStorageCanvasWallRepository =
  LocalStorageCanvasWallRepositoryImpl(LocalStorageCanvasWallDatasourceImpl());

  // this.serviceCanvasWallRepository
  CanvasWallBloc() : super(const CanvasWallState()) {
    on<InitCanvasWall>((event, emit)  => emit(state.copyWith(loading: false, add: false, error: '')));
    on<LoadCanvasWall>(_loadCanvasWall);
    on<SaveCanvasWall>(_saveCanvasWall);
    on<DeleteCanvasWall>(_deleteCanvasWall);
  }

  void _loadCanvasWall(LoadCanvasWall event, Emitter<CanvasWallState> emit) async {
    emit(state.copyWith(loading: true));
    
    final listResp = await localStorageCanvasWallRepository.loadCanvasWall();
    int canvasWallPending = 0;
    int canvasWallSync = 0;

    for(int i=0; i<listResp.length; i++) {
      if(listResp[i].isInSync) {
        canvasWallSync++;
      } else {
        canvasWallPending++;
      }
    }

    // listResp.forEach((canvasWall) {
    //   if(canvasWall.isInSync) {
    //     canvasWallSync++;
    //   } else {
    //     canvasWallPending++;
    //   }
    // });

    emit(state.copyWith(listCanvasWall: listResp, canvasWallPending: canvasWallPending, canvasWallSync: canvasWallSync, loading: false));
  }

  void _saveCanvasWall(SaveCanvasWall event, Emitter<CanvasWallState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      await localStorageCanvasWallRepository.saveCanvasWall(event.canvasWall);
      emit(state.copyWith(loading: false, add: true));
      add(const LoadCanvasWall());
    } catch (error) {
      try {
        if (error.toString().contains('Unique index violated')) {
          emit(state.copyWith(loading: false, error: 'Ya registrado'));
        }
      } catch (e) {
        emit(state.copyWith(loading: false, error: 'Ocurrio un error'));
      }
    }
  }

  void _deleteCanvasWall(DeleteCanvasWall event, Emitter<CanvasWallState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      await localStorageCanvasWallRepository.deleteCanvasWall(event.isarId);
      emit(state.copyWith(loading: false));
      add(const LoadCanvasWall());
    } catch (error) {
      try {
        emit(state.copyWith(loading: false, error: 'Ya registrado'));
      } catch (e) {
        emit(state.copyWith(loading: false, error:'Ocurrio un error'));
      }
    }
  }
}
