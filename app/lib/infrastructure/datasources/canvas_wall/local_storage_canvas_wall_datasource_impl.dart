
import 'package:drift/drift.dart';
import 'package:electorama_app/domain/datasources/canvas_wall/local_storage_canvas_wall_datasource.dart';
import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';
import 'package:electorama_app/infrastructure/datasources/storage/app_database.dart';

class LocalStorageCanvasWallDatasourceImpl
    extends LocalStorageCanvasWallDatasource {
  final AppDatabase _db = appDatabase;

  CanvasWall _toEntity(CanvasWallRow row) => CanvasWall(
        uuid: row.uuid,
        deviceId: row.deviceId,
        latitude: row.latitude,
        longitude: row.longitude,
        projectId: row.projectId,
        categoryId: row.categoryId,
        date: row.date,
        file: row.file, // Uint8List es un List<int>
        isInSync: row.isInSync,
      )..isarId = row.id;

  @override
  Future<List<CanvasWall>> loadCanvasWall() async {
    final rows = await _db.select(_db.canvasWalls).get();
    return rows.map(_toEntity).toList();
  }

  @override
  Future<CanvasWall> saveCanvasWall(CanvasWall canvasWall) async {
    final id = await _db.into(_db.canvasWalls).insert(
          CanvasWallsCompanion.insert(
            uuid: canvasWall.uuid,
            deviceId: canvasWall.deviceId,
            latitude: canvasWall.latitude,
            longitude: canvasWall.longitude,
            projectId: canvasWall.projectId,
            categoryId: canvasWall.categoryId,
            date: canvasWall.date,
            file: Value(canvasWall.file == null
                ? null
                : Uint8List.fromList(canvasWall.file!)),
            isInSync: Value(canvasWall.isInSync),
          ),
        );
    canvasWall.isarId = id;
    return canvasWall;
  }

  @override
  Future<bool> deleteCanvasWall(int isarId) async {
    final count = await (_db.delete(_db.canvasWalls)
          ..where((t) => t.id.equals(isarId)))
        .go();
    return count > 0;
  }

  @override
  Future<CanvasWall> getCanvasWallById(int isarId) async {
    final row = await (_db.select(_db.canvasWalls)
          ..where((t) => t.id.equals(isarId)))
        .getSingle();
    return _toEntity(row);
  }

  @override
  Future<CanvasWall> syncCanvasWall(int isarId) async {
    // Marca como sincronizado y libera el blob de la foto.
    await (_db.update(_db.canvasWalls)..where((t) => t.id.equals(isarId)))
        .write(const CanvasWallsCompanion(
      isInSync: Value(true),
      file: Value(null),
    ));
    return getCanvasWallById(isarId);
  }
}
