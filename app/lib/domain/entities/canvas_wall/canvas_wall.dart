
class CanvasWall {
  int? isarId;

  final String uuid;
  final String deviceId;
  final String latitude;
  final String longitude;
  final String projectId;
  final String categoryId;
  final String date;
  List<int>? file;
  bool isInSync;


  CanvasWall({
    required this.uuid,
    required this.deviceId,
    required this.latitude,
    required this.longitude,
    required this.projectId,
    required this.categoryId,
    required this.date,
    required this.file,
    required this.isInSync
  });

}
