import 'dart:convert';

FollowerModel followerModelFromJson(String str) =>
    FollowerModel.fromJson(json.decode(str));

String followerModelToJson(FollowerModel data) => json.encode(data.toJson());

class FollowerModel {
  final String street;
  final String externalNumber;
  final String? internalNumber;
  final String? zipCode;
  final String? neighborhood;
  final String? sectionNumber;
  final String? sectionNumberIne;

  final int? residenceEntityId;
  final int? residenceMunicipalityId;

  final bool? isAdult;
  final String? names;
  final String? lastName;
  final String? surName;

  final String? curp;
  final String? ine;
  final String? validity;

  final String? cellPhoneNumber;
  final String? supportWhatsapp;
  final String? cellPhoneNumberOther;
  final String? email;

  final String supportsMovement;
  final String? committeeParticipation;
  final String? pollinStationRepresentativeParticipation;
  final String? invitingOthersToParticipate;
  final String? numberOfPeopleInvited;

  final CatSex? catSex;
  final String? birthdate;
  final int? birthEntityId;

  final String latitude;
  final String longitude;

  final String userCreateId;
  final String uuid;
  final String deviceId;
  final bool isInSync;

  FollowerModel({
    required this.street,
    required this.externalNumber,
    required this.internalNumber,
    required this.zipCode,
    required this.neighborhood,
    required this.sectionNumber,
    required this.sectionNumberIne,
    required this.residenceEntityId,
    required this.residenceMunicipalityId,
    required this.isAdult,
    required this.names,
    required this.lastName,
    required this.surName,
    required this.curp,
    required this.ine,
    required this.validity,
    required this.cellPhoneNumber,
    required this.supportWhatsapp,
    required this.cellPhoneNumberOther,
    required this.email,
    required this.supportsMovement,
    required this.committeeParticipation,
    required this.pollinStationRepresentativeParticipation,
    required this.invitingOthersToParticipate,
    required this.numberOfPeopleInvited,
    required this.catSex,
    required this.birthdate,
    required this.birthEntityId,
    required this.latitude,
    required this.longitude,
    required this.userCreateId,
    required this.uuid,
    required this.deviceId,
    required this.isInSync,
  });

  factory FollowerModel.fromJson(Map<String, dynamic> json) => FollowerModel(
        street: json["street"],
        externalNumber: json["externalNumber"],
        internalNumber: json["internalNumber"],
        zipCode: json["zipCode"],
        neighborhood: json["neighborhood"],
        sectionNumber: json["sectionNumber"],
        sectionNumberIne: json["sectionNumberIne"],
        residenceEntityId: json["residenceEntityId"],
        residenceMunicipalityId: json["residenceMunicipalityId"],
        isAdult: json["isAdult"],
        names: json["names"],
        lastName: json["lastName"],
        surName: json["surName"],
        curp: json["curp"],
        ine: json["ine"],
        validity: json["validity"],
        cellPhoneNumber: json["cellPhoneNumber"],
        supportWhatsapp: json["supportWhatsapp"],
        cellPhoneNumberOther: json["cellPhoneNumberOther"],
        email: json["email"],
        supportsMovement: json["supportsMovement"],
        committeeParticipation: json["committeeParticipation"],
        pollinStationRepresentativeParticipation:
            json["pollinStationRepresentativeParticipation"],
        invitingOthersToParticipate: json["invitingOthersToParticipate"],
        numberOfPeopleInvited: json["numberOfPeopleInvited"],
        catSex: json["catSex"] == null ? null : CatSex.fromJson(json["catSex"]),
        birthdate: json["birthdate"],
        birthEntityId: json["birthEntityId"],
        latitude: json["latitude"],
        longitude: json["longitude"],
        userCreateId: json["userCreateId"],
        uuid: json["uuid"],
        deviceId: json["deviceId"],
        isInSync: json["isInSync"],
      );

  Map<String, dynamic> toJson() => {
        "uuid": uuid,
        "deviceId": deviceId,
        "latitude": latitude,
        "longitude": longitude,
        "sectionNumber": sectionNumber,
        "street": street,
        "externalNumber": externalNumber,
        "internalNumber": internalNumber,
        "zipCode": zipCode,
        "neighborhood": neighborhood,
        "sectionNumberIne": sectionNumberIne,
        "residenceEntityId": residenceEntityId,
        "residenceMunicipalityId": residenceMunicipalityId,
        "isAdult": isAdult,
        "names": names,
        "lastName": lastName,
        "surName": surName,
        "curp": curp,
        "ine": ine,
        "validity": validity,
        "birthdate": birthdate,
        "cellPhoneNumber": cellPhoneNumber,
        "supportWhatsapp": supportWhatsapp,
        "cellPhoneNumberOther": cellPhoneNumberOther,
        "email": email,
        "catSex": catSex?.toJson(),
        "birthEntityId": birthEntityId,
        "isInSync": isInSync,
        "supportsMovement": supportsMovement,
        "committeeParticipation": committeeParticipation,
        "pollinStationRepresentativeParticipation":
            pollinStationRepresentativeParticipation,
        "invitingOthersToParticipate": invitingOthersToParticipate,
        "numberOfPeopleInvited": numberOfPeopleInvited,
        "userCreateId": userCreateId,
      };
}

class CatSex {
  final int? id;
  final String? nombre;

  CatSex({this.id, this.nombre});

  factory CatSex.fromJson(Map<String, dynamic> json) =>
      CatSex(id: json["id"], nombre: json["nombre"]);

  Map<String, dynamic> toJson() => {"id": id, "nombre": nombre};
}
