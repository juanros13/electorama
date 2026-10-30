import 'package:dio/dio.dart';
import 'package:electorama_app/domain/datasources/canvas_wall/service_canvas_wall_datasource.dart';
import 'package:electorama_app/domain/repositories/canvas_wall/service_canvas_wall_repository.dart';

class ServiceCanvasWallRepositoryImpl extends ServiceCanvasWallRepository {

  final ServiceCanvasWallDatasource datasource;

  ServiceCanvasWallRepositoryImpl( this.datasource );

  @override
  Future<bool> postCanvasWall(FormData canvasWallFormData, int isarId) {
    return datasource.postCanvasWall(canvasWallFormData, isarId);
  }
  
}