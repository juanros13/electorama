import 'dart:io';
import 'package:timezone/standalone.dart' as tz;
import 'package:device_info_plus/device_info_plus.dart';
import 'package:electorama_app/domain/entities/visits/visit_people.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people_form/visits_people_form_bloc.dart';
import 'package:uuid/uuid.dart';

class VisitPeopleMapper {
  static Future<VisitPeople> castToEntity(VisitPeopleFormState visitPeopleState) async {
    var uuid = const Uuid().v4();
    //DateTime now = DateTime.now();
    var detroit = tz.getLocation('America/Mexico_City');
    var now = tz.TZDateTime.now(detroit);
    if(!visitPeopleState.openTheDoor){
      return VisitPeople(
        uuid: uuid,
        deviceId: await _getId(),
        openTheDoor: visitPeopleState.openTheDoor, 
        street: visitPeopleState.street.value, 
        externallNumber: visitPeopleState.externalNumber.value, 
        longitude: visitPeopleState.latitude, 
        latitude: visitPeopleState.longitude, 
        
        visitNumber: '1', 
        created: now,
        syncVisit: visitPeopleState.syncVisit
      );
    }
    return VisitPeople(
        uuid: uuid,
        deviceId: await _getId(),
        openTheDoor: visitPeopleState.openTheDoor, 
        street: visitPeopleState.street.value, 
        externallNumber: visitPeopleState.externalNumber.value, 
        longitude: visitPeopleState.latitude, 
        latitude: visitPeopleState.longitude, 
        visitNumber: '1',
        age: visitPeopleState.age?.value,
        names: visitPeopleState.names?.value,
        lastName: visitPeopleState.lastName?.value,
        surName: visitPeopleState.surName?.value,
        phone: visitPeopleState.phone?.value,
        acceptInformation: visitPeopleState.acceptInformation,
        sex: visitPeopleState.sex.name,
        hasSympathy: visitPeopleState.hasSympathy,
        created: now,
        syncVisit: visitPeopleState.syncVisit
      );
  }
  // static VisitPeople castToEntityExtend(VisitPeopleState visitPeopleState) =>
  //   VisitPeople(
  //     openTheDoor: visitPeopleState.openTheDoor, 
  //     street: visitPeopleState.street.value, 
  //     internalNumber: visitPeopleState.externalNumber.value, 
  //     longitude: visitPeopleState.latitude, 
  //     latitude: visitPeopleState.longitude, 
  //   );
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