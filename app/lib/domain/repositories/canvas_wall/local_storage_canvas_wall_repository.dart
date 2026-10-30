import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';

abstract class LocalStorageCanvasWallRepository {

  Future<List<CanvasWall>> loadCanvasWall();
  Future<CanvasWall> saveCanvasWall(CanvasWall canvasWall);
  Future<bool> deleteCanvasWall(int isarId);
  Future<CanvasWall> getCanvasWallById(int isarId);
  Future<CanvasWall> syncCanvasWall(int isarId);

}