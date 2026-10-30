import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:uuid/uuid.dart';
import 'package:timezone/standalone.dart' as tz;

class FollowerMapper {
  static Future<Follower> castToEntity(FollowerFormState s) async {
    final uuid = const Uuid().v4();
    final mex = tz.getLocation('America/Mexico_City');
    final now = tz.TZDateTime.now(mex);

    if (!s.supportsMovement) {
      return Follower(
        uuid: uuid,
        deviceId: await _getId(),
        created: now,
        isInSync: false,
        positionId: s.positionId,
        latitude: s.latitude,
        longitude: s.longitude,
        birthEntityId: 27, // 27 = TABASCO
        residenceEntityId: 27,
        residenceMunicipalityId: int.tryParse(s.residenceMunicipality.value),
        catSex: s.catSex,
        sectionNumber: s.sectionNumber.value,
        street: s.street.value,
        externalNumber: s.externalNumber.value,
        internalNumber:
            s.internalNumber.value == '' ? null : s.internalNumber.value,
        zipCode: s.zipCode.value == '' ? null : s.zipCode.value,
        supportsMovement: 'NO',
        neighborhood: s.neighborhood.value == '' ? null : s.neighborhood.value,
      );
    } else {
      return Follower(
        uuid: uuid,
        deviceId: await _getId(),
        created: now,
        isInSync: false,
        positionId: s.positionId,
        latitude: s.latitude,
        longitude: s.longitude,
        names: s.names.value,
        lastName: s.lastName.value,
        surName: s.surName.value,
        curp: s.curp.value == '' ? null : s.curp.value,
        birthdate: s.birthday,
        catSex: s.catSex,
        cellPhoneNumber: s.cellPhoneNumber.value,
        birthEntityId: 27,
        sectionNumberIne: s.sectionNumberIne,
        cellPhoneNumberOther: s.cellPhoneNumberOther.value == ''
            ? null
            : s.cellPhoneNumberOther.value,
        sectionNumber: s.sectionNumber.value,
        street: s.street.value,
        externalNumber: s.externalNumber.value,
        internalNumber:
            s.internalNumber.value == '' ? null : s.internalNumber.value,
        zipCode: s.zipCode.value == '' ? null : s.zipCode.value,
        residenceEntityId: 27,
        residenceMunicipalityId: int.tryParse(s.residenceMunicipality.value),
        supportsWhatsapp: s.supportsWhatsapp ? 'SI' : 'NO',
        supportsMovement: 'SI',
        committeeParticipation: s.committeeParticipation ? 'SI' : 'NO',
        pollinStationRepresentativeParticipation:
            s.pollinStationRepresentativeParticipation ? 'SI' : 'NO',
        invitingOthersToParticipate:
            s.invitingOthersToParticipate ? 'SI' : 'NO',
        numberOfPeopleInvited: s.numberOfPeopleInvited.name,
        neighborhood: s.neighborhood.value == '' ? null : s.neighborhood.value,
        ine: s.ine.value == '' ? null : s.ine.value,
        isAdult: s.isLegalAge,
        email: s.email.value == '' ? null : s.email.value,
        validity:
            s.expirationYear.value == '' ? null : s.expirationYear.value,
      );
    }
  }

  static Future<String> _getId() async {
    final deviceInfo = DeviceInfoPlugin();
    if (Platform.isIOS) {
      final ios = await deviceInfo.iosInfo;
      return ios.identifierForVendor.toString();
    } else if (Platform.isAndroid) {
      final android = await deviceInfo.androidInfo;
      return android.id;
    }
    return '';
  }
}
