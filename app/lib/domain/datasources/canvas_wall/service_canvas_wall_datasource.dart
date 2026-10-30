import 'package:dio/dio.dart';

abstract class ServiceCanvasWallDatasource {

  Future<bool> postCanvasWall(FormData canvasWallJson, int isarId);

}