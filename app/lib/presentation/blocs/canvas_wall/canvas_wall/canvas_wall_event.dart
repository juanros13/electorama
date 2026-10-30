part of 'canvas_wall_bloc.dart';

abstract class CanvasWallEvent {
  const CanvasWallEvent();
}

class InitCanvasWall extends CanvasWallEvent {}

class LoadCanvasWall extends CanvasWallEvent {
  const LoadCanvasWall();
}

class SaveCanvasWall extends CanvasWallEvent {
  final CanvasWall canvasWall;
  const SaveCanvasWall({
    required this.canvasWall,
  });
}

class DeleteCanvasWall extends CanvasWallEvent {
  final int isarId;
  const DeleteCanvasWall({
    required this.isarId
  });
}