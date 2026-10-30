import 'package:drift/drift.dart';
import 'package:flutter/foundation.dart';
import 'package:electorama_app/domain/datasources/storage/local_storage_datasource.dart';
import 'package:electorama_app/domain/entities/visits/visit_people.dart';
import 'package:electorama_app/infrastructure/datasources/storage/app_database.dart';

class LocalStorageVisitDataSourceImpl extends LocalStorageDatasource {
  final AppDatabase _db = appDatabase;

  VisitPeople _toEntity(VisitPeopleRow row) => VisitPeople(
        uuid: row.uuid,
        deviceId: row.deviceId,
        openTheDoor: row.openTheDoor,
        street: row.street,
        externallNumber: row.externallNumber,
        longitude: row.longitude,
        latitude: row.latitude,
        visitNumber: row.visitNumber,
        syncVisit: row.syncVisit,
        age: row.age,
        names: row.names,
        lastName: row.lastName,
        surName: row.surName,
        phone: row.phone,
        acceptInformation: row.acceptInformation,
        sex: row.sex,
        hasSympathy: row.hasSympathy,
        created: row.created,
      )..isarId = row.id;

  @override
  Future<List<VisitPeople>> loadVisits() async {
    final rows = await _db.select(_db.visitPeoples).get();
    return rows.map(_toEntity).toList();
  }

  @override
  Future<VisitPeople> insertVisitPeople(VisitPeople visitPeople) async {
    final id = await _db.into(_db.visitPeoples).insert(
          VisitPeoplesCompanion.insert(
            uuid: visitPeople.uuid,
            deviceId: visitPeople.deviceId,
            openTheDoor: Value(visitPeople.openTheDoor),
            street: visitPeople.street,
            externallNumber: visitPeople.externallNumber,
            longitude: visitPeople.longitude,
            latitude: visitPeople.latitude,
            visitNumber: visitPeople.visitNumber,
            syncVisit: Value(visitPeople.syncVisit),
            age: Value(visitPeople.age),
            names: Value(visitPeople.names),
            lastName: Value(visitPeople.lastName),
            surName: Value(visitPeople.surName),
            phone: Value(visitPeople.phone),
            acceptInformation: Value(visitPeople.acceptInformation),
            sex: Value(visitPeople.sex),
            hasSympathy: Value(visitPeople.hasSympathy),
            created: Value(visitPeople.created),
          ),
        );
    visitPeople.isarId = id;
    return visitPeople;
  }

  @override
  Future<bool> deleteVisitPeople(int isarId) async {
    final count = await (_db.delete(_db.visitPeoples)
          ..where((t) => t.id.equals(isarId)))
        .go();
    return count > 0;
  }

  @override
  Future<VisitPeople> getVisitPeopleById(int isarId) async {
    final row = await (_db.select(_db.visitPeoples)
          ..where((t) => t.id.equals(isarId)))
        .getSingle();
    return _toEntity(row);
  }

  @override
  Future<void> postVisited(Map<String, dynamic> visitedJson) async {
    debugPrint(visitedJson.toString());
    throw ErrorDescription("delete this.");
  }

  @override
  Future<VisitPeople> syncVisit(int isarId) async {
    await (_db.update(_db.visitPeoples)..where((t) => t.id.equals(isarId)))
        .write(const VisitPeoplesCompanion(syncVisit: Value(true)));
    return getVisitPeopleById(isarId);
  }
}
