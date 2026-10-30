class Follower {
  int? id;

  final String uuid;
  final String deviceId;

  final String latitude;
  final String longitude;

  final String sectionNumber;
  final String street;
  final String externalNumber;
  final String? internalNumber;
  final String? zipCode;
  final String? neighborhood;
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

  final String? birthdate;
  final String? cellPhoneNumber;
  final String? supportsWhatsapp;
  final String? cellPhoneNumberOther;
  final String? email;

  final Sex? catSex;
  final int? birthEntityId;

  bool isInSync;

  final String supportsMovement;
  final String? committeeParticipation;
  final String? pollinStationRepresentativeParticipation;
  final String? invitingOthersToParticipate;
  final String? numberOfPeopleInvited;

  final DateTime? created;

  /// El puesto donde queda (capture-targets); null = el de quien captura.
  final int? positionId;
  /// PENDING, SYNCED, DUPLICATE o REJECTED (ver [FollowerSyncStatus]).
  final String syncStatus;
  /// Lo que dijo el servidor si no lo aceptó o ya existía.
  final String? syncError;
  final int? membershipId;
  /// Dónde quedó en la estructura: "S-0233A1-11".
  final String? positionLabel;

  Follower({
    this.id,
    required this.uuid,
    required this.deviceId,
    required this.latitude,
    required this.longitude,
    required this.sectionNumber,
    required this.street,
    required this.externalNumber,
    this.internalNumber,
    this.zipCode,
    this.neighborhood,
    this.sectionNumberIne,
    this.residenceEntityId,
    this.residenceMunicipalityId,
    this.isAdult,
    this.names,
    this.lastName,
    this.surName,
    this.curp,
    this.ine,
    this.email,
    this.validity,
    this.cellPhoneNumber,
    this.supportsWhatsapp,
    this.cellPhoneNumberOther,
    this.birthEntityId,
    required this.supportsMovement,
    this.committeeParticipation,
    this.pollinStationRepresentativeParticipation,
    this.invitingOthersToParticipate,
    this.numberOfPeopleInvited,
    this.birthdate,
    this.catSex,
    required this.isInSync,
    this.created,
    this.positionId,
    this.syncStatus = FollowerSyncStatus.pending,
    this.syncError,
    this.membershipId,
    this.positionLabel,
  });

  /// Pendiente de mandar (o rechazado: se corrige o se borra).
  bool get needsAttention =>
      syncStatus == FollowerSyncStatus.pending ||
      syncStatus == FollowerSyncStatus.rejected;
}

/// Cómo terminó el envío de un convencido al servidor.
class FollowerSyncStatus {
  static const pending = 'PENDING';
  static const synced = 'SYNCED';
  /// Ya era convencido en la estructura: no se duplicó.
  static const duplicate = 'DUPLICATE';
  /// El servidor no lo aceptó (sección, ubicación, CURP…): hay que corregirlo.
  static const rejected = 'REJECTED';
}

class Sex {
  final int? id;
  final String? nombre;
  const Sex({this.id, this.nombre});

  @override
  bool operator ==(Object other) =>
      other is Sex && other.id == id && other.nombre == nombre;

  @override
  int get hashCode => Object.hash(id, nombre);
}
