
import 'package:electorama_app/domain/datasources/storage/local_storage_datasource.dart';
import 'package:electorama_app/domain/entities/visits/visit_people.dart';
import 'package:electorama_app/domain/repositories/storage/local_storage_repository.dart';

class LocalStorageRepositoryImpl extends LocalStorageRepository {

  final LocalStorageDatasource datasource;

  LocalStorageRepositoryImpl( this.datasource );

  @override
  Future<List<VisitPeople>> loadVisits() {
    return datasource.loadVisits();
  }

  @override
  Future<VisitPeople> insertVisitPeople(VisitPeople visitPeople) {
    return datasource.insertVisitPeople(visitPeople);
  }
  
  @override
  Future<bool> deleteVisitPeople(int isarId) {
    return datasource.deleteVisitPeople(isarId);
  }
  
  @override
  Future<VisitPeople> getVisitPeopleById(int isarId) {
    return datasource.getVisitPeopleById(isarId);
  }

  @override
  Future<void> postVisited(Map<String, dynamic> visitedJson) {
    return datasource.postVisited(visitedJson);
  }
  
  @override
  Future<VisitPeople> syncVisit(int isarId) {
    return datasource.syncVisit(isarId);
  }
}