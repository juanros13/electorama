import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall_form/canvas_wall_form_bloc.dart';
import 'package:intl/intl.dart';
import 'package:uuid/uuid.dart';
import 'package:timezone/standalone.dart' as tz;

class CanvasWallMapper {

  static Future<CanvasWall> castToEntity(CanvasWallFormState canvasWallFormState) async {

    var uuid = const Uuid().v4();
    
    var mex = tz.getLocation('America/Mexico_City');
    final dateToday = DateFormat('yyyy-MM-dd').format( tz.TZDateTime.from(DateTime.now(), mex) );

    return CanvasWall(
      uuid: uuid, 
      deviceId: await _getId(), 
      latitude: canvasWallFormState.latitude, 
      longitude: canvasWallFormState.longitude, 
      projectId: canvasWallFormState.projectSelected.value, 
      categoryId: canvasWallFormState.categorySelected.value, 
      date: dateToday, 
      isInSync: false,
      file:  canvasWallFormState.photo!,
    );

  }

}

Future<String> _getId() async {
  var deviceInfo = DeviceInfoPlugin();
  if (Platform.isIOS) { // import 'dart:io'
    var iosDeviceInfo = await deviceInfo.iosInfo;
    return iosDeviceInfo.identifierForVendor.toString(); // unique ID on iOS
  } else if(Platform.isAndroid) {
    var androidDeviceInfo = await deviceInfo.androidInfo;
    return androidDeviceInfo.id; // unique ID on Android
  } 
  return '';
}