part of 'canvas_wall_service_bloc.dart';

abstract class CanvasWallServiceEvent {
  const CanvasWallServiceEvent();
}

// class InitCanvasWallService extends CanvasWallServiceEvent {}

class SentCanvasWallService extends CanvasWallServiceEvent {
  final String section;
  final String municipality; 
  final String locality;
  final String coordinateZoneId;
  final String referenceAuthId;

  const SentCanvasWallService({
    required this.section,
    required this.municipality,
    required this.locality,
    required this.coordinateZoneId,
    required this.referenceAuthId
  });
}

class ChangeListCanvasWall extends CanvasWallServiceEvent {
  final List<CanvasWall> listCanvasWall;

  ChangeListCanvasWall({
    required this.listCanvasWall
  });

}

class ChangeSubmitCanvasWallSync extends CanvasWallServiceEvent {
  final bool value;

  ChangeSubmitCanvasWallSync({
    required this.value
  });

}