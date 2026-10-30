part of 'canvas_wall_service_bloc.dart';

class CanvasWallServiceState extends Equatable {

  final bool loading;
  final String error;
  final bool submitSync;
  final List<CanvasWall> listCanvasWall;
  final int canvasWallSync;
  final int canvasWallError;

  const CanvasWallServiceState({
    this.loading = false,
    this.error = '',
    this.submitSync = false,
    this.listCanvasWall = const [],
    this.canvasWallSync = 0,
    this.canvasWallError = 0,
  });

  CanvasWallServiceState copyWith({
    bool? loading,
    String? error,
    bool? submitSync,
    List<CanvasWall>? listCanvasWall,
    int? canvasWallSync,
    int? canvasWallError,
  }) => CanvasWallServiceState(
    loading: loading ?? this.loading,
    error: error ?? this.error,
    submitSync: submitSync ?? this.submitSync,
    listCanvasWall: listCanvasWall ?? this.listCanvasWall,
    canvasWallSync: canvasWallSync ?? this.canvasWallSync,
    canvasWallError: canvasWallError ?? this.canvasWallError,
  );


  @override
  List<Object> get props => [
    loading,
    error,
    submitSync,
    listCanvasWall,
    canvasWallSync,
    canvasWallError,
  ];

}

class CanvasWallServiceInitial extends CanvasWallServiceState {}