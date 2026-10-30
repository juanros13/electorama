part of 'canvas_wall_bloc.dart';

class CanvasWallState extends Equatable {

  final bool loading;
  final String error;
  final bool add;
  final List<CanvasWall> listCanvasWall;
  final int canvasWallPending;
  final int canvasWallSync;


  const CanvasWallState({
    this.loading = false,
    this.error = '',
    this.add = false,
    this.listCanvasWall = const [],
    this.canvasWallPending = 0,
    this.canvasWallSync = 0,
  });

  CanvasWallState copyWith({
    bool? loading,
    String? error,
    bool? add,
    List<CanvasWall>? listCanvasWall,
    int? canvasWallPending,
    int? canvasWallSync,
  }) => CanvasWallState(
    loading: loading ?? this.loading,
    error: error ?? this.error,
    add: add ?? this.add,
    listCanvasWall: listCanvasWall ?? this.listCanvasWall,
    canvasWallPending: canvasWallPending ?? this.canvasWallPending,
    canvasWallSync: canvasWallSync ?? this.canvasWallSync,
  );


  @override
  List<Object> get props => [
    loading,
    error,
    add,
    listCanvasWall,
    canvasWallPending,
    canvasWallSync
  ];

}

class CanvasWallInitial extends CanvasWallState {}