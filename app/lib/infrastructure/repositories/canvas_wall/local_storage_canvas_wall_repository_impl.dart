

import 'package:electorama_app/domain/datasources/canvas_wall/local_storage_canvas_wall_datasource.dart';
import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';
import 'package:electorama_app/domain/repositories/canvas_wall/local_storage_canvas_wall_repository.dart';

class LocalStorageCanvasWallRepositoryImpl extends LocalStorageCanvasWallRepository {
  
  final LocalStorageCanvasWallDatasource datasource;

  LocalStorageCanvasWallRepositoryImpl( this.datasource );


  @override
  Future<bool> deleteCanvasWall(int isarId) {
    return datasource.deleteCanvasWall(isarId);
  }

  @override
  Future<CanvasWall> getCanvasWallById(int isarId) {
    return datasource.getCanvasWallById(isarId);
  }

  @override
  Future<List<CanvasWall>> loadCanvasWall() {
    return datasource.loadCanvasWall();
  }

  @override
  Future<CanvasWall> saveCanvasWall(CanvasWall canvasWall) {
    return datasource.saveCanvasWall(canvasWall);
  }
  
  @override
  Future<CanvasWall> syncCanvasWall(int isarId) {
    return datasource.syncCanvasWall(isarId);
  }


}