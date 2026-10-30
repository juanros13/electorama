import 'package:drift/drift.dart';
import 'package:electorama_app/domain/datasources/follower/local_storage_followers_datasource.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/infrastructure/datasources/storage/app_database.dart';

class LocalStorageFollowerDatasourceImpl
    implements LocalStorageFollowersDatasource {
  final AppDatabase _db = appDatabase;

  Follower _toEntity(FollowerRow row) => Follower(
        id: row.id,
        uuid: row.uuid,
        deviceId: row.deviceId,
        latitude: row.latitude,
        longitude: row.longitude,
        sectionNumber: row.sectionNumber,
        street: row.street,
        externalNumber: row.externalNumber,
        internalNumber: row.internalNumber,
        zipCode: row.zipCode,
        neighborhood: row.neighborhood,
        sectionNumberIne: row.sectionNumberIne,
        residenceEntityId: row.residenceEntityId,
        residenceMunicipalityId: row.residenceMunicipalityId,
        isAdult: row.isAdult,
        names: row.names,
        lastName: row.lastName,
        surName: row.surName,
        curp: row.curp,
        ine: row.ine,
        validity: row.validity,
        birthdate: row.birthdate,
        cellPhoneNumber: row.cellPhoneNumber,
        supportsWhatsapp: row.supportsWhatsapp,
        cellPhoneNumberOther: row.cellPhoneNumberOther,
        email: row.email,
        catSex: (row.catSexId == null && row.catSexNombre == null)
            ? null
            : Sex(id: row.catSexId, nombre: row.catSexNombre),
        birthEntityId: row.birthEntityId,
        isInSync: row.isInSync,
        supportsMovement: row.supportsMovement,
        committeeParticipation: row.committeeParticipation,
        pollinStationRepresentativeParticipation:
            row.pollinStationRepresentativeParticipation,
        invitingOthersToParticipate: row.invitingOthersToParticipate,
        numberOfPeopleInvited: row.numberOfPeopleInvited,
        created: row.created,
        positionId: row.positionId,
        syncStatus: row.syncStatus,
        syncError: row.syncError,
        membershipId: row.membershipId,
        positionLabel: row.positionLabel,
      );

  FollowersCompanion _toCompanion(Follower f) => FollowersCompanion.insert(
        uuid: f.uuid,
        deviceId: f.deviceId,
        latitude: f.latitude,
        longitude: f.longitude,
        sectionNumber: f.sectionNumber,
        street: f.street,
        externalNumber: f.externalNumber,
        supportsMovement: f.supportsMovement,
        internalNumber: Value(f.internalNumber),
        zipCode: Value(f.zipCode),
        neighborhood: Value(f.neighborhood),
        sectionNumberIne: Value(f.sectionNumberIne),
        residenceEntityId: Value(f.residenceEntityId),
        residenceMunicipalityId: Value(f.residenceMunicipalityId),
        isAdult: Value(f.isAdult),
        names: Value(f.names),
        lastName: Value(f.lastName),
        surName: Value(f.surName),
        curp: Value(f.curp),
        ine: Value(f.ine),
        validity: Value(f.validity),
        birthdate: Value(f.birthdate),
        cellPhoneNumber: Value(f.cellPhoneNumber),
        supportsWhatsapp: Value(f.supportsWhatsapp),
        cellPhoneNumberOther: Value(f.cellPhoneNumberOther),
        email: Value(f.email),
        catSexId: Value(f.catSex?.id),
        catSexNombre: Value(f.catSex?.nombre),
        birthEntityId: Value(f.birthEntityId),
        isInSync: Value(f.isInSync),
        committeeParticipation: Value(f.committeeParticipation),
        pollinStationRepresentativeParticipation:
            Value(f.pollinStationRepresentativeParticipation),
        invitingOthersToParticipate: Value(f.invitingOthersToParticipate),
        numberOfPeopleInvited: Value(f.numberOfPeopleInvited),
        created: Value(f.created),
        positionId: Value(f.positionId),
        syncStatus: Value(f.syncStatus),
      );

  @override
  Future<List<Follower>> loadFollowers() async {
    final rows = await _db.select(_db.followers).get();
    return rows.map(_toEntity).toList();
  }

  @override
  Future<List<Follower>> loadFollowersByRangeOfDates(
      DateTime startDate, DateTime endDate) async {
    final rows = await (_db.select(_db.followers)
          ..where((t) => t.created.isBetweenValues(startDate, endDate)))
        .get();
    return rows.map(_toEntity).toList();
  }

  @override
  Future<Follower> saveFollower(Follower follower) async {
    final id = await _db.into(_db.followers).insert(_toCompanion(follower));
    follower.id = id;
    return follower;
  }

  @override
  Future<bool> deleteFollower(int isarId) async {
    final count = await (_db.delete(_db.followers)
          ..where((t) => t.id.equals(isarId)))
        .go();
    return count > 0;
  }

  @override
  Future<Follower> getFollowerById(int isarId) async {
    final row = await (_db.select(_db.followers)
          ..where((t) => t.id.equals(isarId)))
        .getSingle();
    return _toEntity(row);
  }

  /// Guarda cómo terminó el envío. SYNCED y DUPLICATE ya no se vuelven a mandar.
  Future<void> markSyncResult(
    int id, {
    required String status,
    String? error,
    int? membershipId,
    String? positionLabel,
  }) async {
    final done = status == FollowerSyncStatus.synced ||
        status == FollowerSyncStatus.duplicate;
    await (_db.update(_db.followers)..where((t) => t.id.equals(id))).write(
      FollowersCompanion(
        syncStatus: Value(status),
        syncError: Value(error),
        membershipId: Value(membershipId),
        positionLabel: Value(positionLabel),
        isInSync: Value(done),
        syncedAt: Value(done ? DateTime.now() : null),
      ),
    );
  }

  @override
  Future<Follower> syncFollowers(int isarId) async {
    await (_db.update(_db.followers)..where((t) => t.id.equals(isarId)))
        .write(const FollowersCompanion(isInSync: Value(true)));
    return getFollowerById(isarId);
  }
}
