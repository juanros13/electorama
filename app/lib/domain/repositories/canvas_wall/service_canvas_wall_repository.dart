import 'package:dio/dio.dart';

abstract class ServiceCanvasWallRepository {

  Future<bool> postCanvasWall(FormData canvasWallFormData, int isarId);

}